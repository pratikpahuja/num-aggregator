package org.task.aggregator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.task.aggregator.client.ClientCallException;


@Configuration
public class RestClientConfig {

  private static final int CONNECT_TIMEOUT = 10;
  private final int readTimeout;

  public RestClientConfig(@Value("${endpoint.read-timeout-in-ms}") int readTimeout) {
    this.readTimeout = readTimeout;
  }

  @Bean
  public RestClient client() {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(CONNECT_TIMEOUT);
    factory.setReadTimeout(readTimeout);

    return RestClient.builder()
      .requestFactory(factory)
      .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
        throw new ClientCallException();
      })
      .build();
  }
}
