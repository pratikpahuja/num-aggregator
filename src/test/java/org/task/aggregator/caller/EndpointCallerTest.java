package org.task.aggregator.caller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.task.aggregator.client.EndpointClient;
import org.task.aggregator.client.EndpointResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EndpointCallerTest {

  private EndpointClient client;
  private EndpointCaller caller;

  @BeforeEach
  void setup() {
    client = mock(EndpointClient.class);
    caller = new EndpointCaller(1, 100, client);
  }

  @Test
  void testCall() throws InterruptedException {
    var response = randomIntegerArray(3);
    configureEndpointResponse("endpoint1", response);

    var result = caller.call(List.of("endpoint1"));

    TimeUnit.MILLISECONDS.sleep(200);

    verify(client).call("endpoint1");
    assertThat(result, is(Map.of("endpoint1", Optional.of(new EndpointResponse(response)))));
  }

  @Test
  void testCallWhenOneIsError() throws InterruptedException {
    var response = randomIntegerArray(3);
    configureEndpointResponse("endpoint1", response);
    configureEndpointErrorResponse("endpoint2");

    var result = caller.call(List.of("endpoint1", "endpoint2"));

    TimeUnit.MILLISECONDS.sleep(200);

    verify(client).call("endpoint1");
    verify(client).call("endpoint2");
    assertThat(result, is(Map.of(
      "endpoint1", Optional.of(new EndpointResponse(response)),
      "endpoint2", Optional.of(EndpointResponse.errorResponse())
    )));
  }

  @Test
  void testCallWhenOneTimeout() throws InterruptedException {
    var response = randomIntegerArray(3);
    configureEndpointResponse("endpoint1", response);
    configureEndpointTimeout("endpoint3");

    var result = caller.call(List.of("endpoint1", "endpoint3"));

    TimeUnit.MILLISECONDS.sleep(200);

    verify(client).call("endpoint1");
    verify(client).call("endpoint3");
    assertThat(result, is(Map.of(
      "endpoint1", Optional.of(new EndpointResponse(response)),
      "endpoint3", Optional.empty()
    )));
  }

  private void configureEndpointResponse(String endpoint, int[] response) {
    when(client.call(endpoint))
      .thenReturn(new EndpointResponse(response));
  }

  private void configureEndpointErrorResponse(String endpoint) {
    when(client.call(endpoint))
      .thenReturn(EndpointResponse.errorResponse());
  }

  private void configureEndpointTimeout(String endpoint) {
    when(client.call(endpoint))
      .thenAnswer(i -> {
        TimeUnit.MILLISECONDS.sleep(150);
        return new int[] {10, 20};
      });
  }

  private int[] randomIntegerArray(int size) {
    var rd = new Random();
    var arr = new int[size];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = rd.nextInt();
    }

    return arr;
  }
}