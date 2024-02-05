package org.task.aggregator.caller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.task.aggregator.client.EndpointClient;
import org.task.aggregator.client.EndpointResponse;
import org.task.aggregator.service.EndpointResponseCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Component
@Slf4j
public class EndpointCaller {

  private final ExecutorService pool;
  private final int poolExecutionTimeout;
  private final EndpointClient client;



  public EndpointCaller(@Value("${endpoint-caller.pool-size}") int poolSize,
                        @Value("${endpoint-caller.pool-execution-timeout-in-ms}") int poolExecutionTimeout,
                        EndpointClient client) {
    this.poolExecutionTimeout = poolExecutionTimeout;
    pool = newFixedThreadPool(poolSize);
    this.client = client;
  }

  public Map<String, Optional<EndpointResponse>> call(List<String> urls) {
    var callables = prepareCallables(urls);

    try {
      return makeClientCall(urls, callables);
    } catch (InterruptedException e) {
      log.error(STR."Exception raised while invoking futures, urls: \{urls}", e);

      return emptyOptionalResult(urls);
    }
  }

  /**
   * Returns map of url with their response as optional.
   * In case the response is not received within acceptable time,
   * the map will contain the corresponding URL with empty optional.
   */
  private Map<String, Optional<EndpointResponse>> makeClientCall(List<String> urls, List<ClientCallTask> callables) throws InterruptedException {
    var futures = pool.invokeAll(callables, poolExecutionTimeout, MILLISECONDS);
    return bindResultWithUrl(urls, futures);
  }

  private static Map<String, Optional<EndpointResponse>> bindResultWithUrl(List<String> urls, List<Future<EndpointResponse>> futures) {
    var resultMap = new HashMap<String, Optional<EndpointResponse>>();

    for (var index = 0; index < urls.size(); index++) {
      var url = urls.get(index);
      var future = futures.get(index);

      if (!future.isCancelled()) {
        resultMap.put(url, extractResult(url, future));
      } else {
        resultMap.put(url, empty());
      }
    }
    return resultMap;
  }

  private static Optional<EndpointResponse> extractResult(String url, Future<EndpointResponse> future) {
    try {
      return of(future.get());
    } catch (InterruptedException | ExecutionException e) {
      log.error(STR."Exception raised while retreiving result for url: \{url}", e);
      return empty();
    }
  }

  private static Map<String, Optional<EndpointResponse>> emptyOptionalResult(List<String> urls) {
    return urls.stream()
      .collect(toMap(identity(), _ -> empty()));
  }

  private List<ClientCallTask> prepareCallables(List<String> urls) {
    return urls.stream()
      .map(url -> new ClientCallTask(url, client))
      .toList();
  }

  private record ClientCallTask(String url, EndpointClient client)
    implements Callable<EndpointResponse> {
    @Override
      public EndpointResponse call() throws Exception {
        return client.call(url);
      }
    }
}
