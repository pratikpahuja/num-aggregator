package org.task.aggregator.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;


@Component
@RequiredArgsConstructor
@Slf4j
public class EndpointClient {

  private final RestClient client;

  /**
   * Returns empty in case of 4xx & 5xx errors.
   * @param url
   * @return
   */
  public EndpointResponse call(String url) {
    try {
      return client.get()
        .uri(url)
        .retrieve()
        .body(EndpointResponse.class);
    } catch (ResourceAccessException e) {
      log.error(STR."Exception raised while calling URL: \{url}", e);
      return EndpointResponse.timeOutResponse();
    } catch (ClientCallException | RestClientException e) {
      log.error(STR."Exception raised while calling URL: \{url}", e);
      return EndpointResponse.errorResponse();
    }
  }


}
