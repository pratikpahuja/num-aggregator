package org.task.aggregator.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.task.aggregator.caller.EndpointCaller;

import java.time.Duration;
import java.util.Optional;

@Service
public class EndpointResponseCache {

  private final Cache<String, int[]> cache;
  private final EndpointCaller caller;

  public EndpointResponseCache(@Value("${cache.size}") int size,
                               @Value("${cache.max-age-in-ms}") int maxAgeInMs,
                               EndpointCaller caller) {
    this.caller = caller;
    cache = prepareCache(size, maxAgeInMs);
  }

  public Optional<int[]> get(String url) {
    return Optional.ofNullable(cache.getIfPresent(url));
  }

  public void put(String url, int[] nums) {
    cache.put(url, nums);
  }

  @Async
  @EventListener
  public void retryEmptyResponseEndpoints(EmptyResponseEndpointList endpoints) {
    caller.call(endpoints.getUrls())
      .entrySet().stream()
      .filter(response -> response.getValue().isPresent())
      .filter(response -> response.getValue().get().isSuccess())
      .forEach(response -> put(response.getKey(), response.getValue().get().getNumbers()));
  }

  private Cache<String, int[]> prepareCache(int size, int maxAgeInMs) {
    return CacheBuilder.newBuilder()
      .maximumSize(size)
      .expireAfterWrite(Duration.ofMillis(maxAgeInMs))
      .build();
  }
}
