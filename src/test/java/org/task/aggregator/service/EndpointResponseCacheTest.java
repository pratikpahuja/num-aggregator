package org.task.aggregator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.task.aggregator.caller.EndpointCaller;
import org.task.aggregator.client.EndpointResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

class EndpointResponseCacheTest {

  private EndpointResponseCache cache;
  private EndpointCaller endpointCaller;

  @BeforeEach
  void setup() {
    endpointCaller = mock(EndpointCaller.class);
    cache = new EndpointResponseCache(2, 100, endpointCaller);
  }

  @Test
  void getIfNotPresent() {
    var result = cache.get("sample");
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  void getIfPresent() {
    var numbers = randomIntegerArray(5);
    cache.put("foo", numbers);

    var result = cache.get("foo");

    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(numbers));
  }

  @Test
  void checkCacheAge() throws InterruptedException {
    cache.put("foo", new int[] {10, 20});
    TimeUnit.MILLISECONDS.sleep(501);

    var result = cache.get("foo");

    assertThat(result.isEmpty(), is(true));
  }

  @Test
  void checkCacheSize() {
    cache.put("foo1", new int[] {10, 20});
    cache.put("foo2", new int[] {30, 40});
    cache.put("foo3", new int[] {40, 50});

    var result1 = cache.get("foo1");
    var result2 = cache.get("foo2");
    var result3 = cache.get("foo3");

    assertThat(result1.isPresent(), is(false)); //Since size = 2, this one does not exists anymore
    assertThat(result2.isPresent(), is(true));
    assertThat(result3.isPresent(), is(true));

    assertThat(result2.get(), is(new int[] {30, 40}));
    assertThat(result3.get(), is(new int[] {40, 50}));
  }

  @Test
  void prepareCacheSizeWhenResponseError() {
    configureEndpointErrorResponse("endpoint1");

    cache.retryEmptyResponseEndpoints(new EmptyResponseEndpointList(List.of("endpoint1")));

    assertThat(cache.get("endpoint1").isEmpty(), is(true));
  }

  @Test
  void prepareCacheSizeWhenResponseEmpty() {
    configureEndpointEmptyResponse("endpoint1");

    cache.retryEmptyResponseEndpoints(new EmptyResponseEndpointList(List.of("endpoint1")));

    assertThat(cache.get("endpoint1").isEmpty(), is(true));
  }

  @Test
  void prepareCacheSizeWhenResponseSuccess() {
    var numbers = randomIntegerArray(5);
    configureEndpointResponse("endpoint1", numbers);

    cache.retryEmptyResponseEndpoints(new EmptyResponseEndpointList(List.of("endpoint1")));

    assertThat(cache.get("endpoint1").isPresent(), is(true));
    assertThat(cache.get("endpoint1").get(), is(numbers));
  }

  private void configureEndpointResponse(String endpoint, int[] response) {
    when(endpointCaller.call(List.of(endpoint)))
      .thenReturn(Map.of(endpoint, Optional.of(new EndpointResponse(response))));
  }

  private void configureEndpointErrorResponse(String endpoint) {
    when(endpointCaller.call(List.of(endpoint)))
      .thenReturn(Map.of(endpoint, Optional.of(EndpointResponse.errorResponse())));
  }

  private void configureEndpointEmptyResponse(String endpoint) {
    when(endpointCaller.call(List.of(endpoint)))
      .thenReturn(Map.of(endpoint, Optional.empty()));
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