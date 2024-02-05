package org.task.aggregator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.task.aggregator.caller.EndpointCaller;
import org.task.aggregator.client.EndpointResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AggregatorServiceTest {

  private AggregatorService service;
  private EndpointCaller endpointCaller;
  private EndpointResponseCache endpointResponseCache;
  private ApplicationEventPublisher appEventPublisher;
  private EndpointResponseCombinator endpointResponseCombinator;

  @BeforeEach
  void setup() {
    endpointCaller = mock(EndpointCaller.class);
    endpointResponseCache = mock(EndpointResponseCache.class);
    appEventPublisher = mock(ApplicationEventPublisher.class);
    endpointResponseCombinator = new EndpointResponseCombinator();
    service = new AggregatorService(endpointCaller, endpointResponseCache, appEventPublisher, endpointResponseCombinator);
  }

  @Test
  void aggregateWhenEmptyList() {
    var actualResult = service.aggregate(List.of());

    assertThat(actualResult, is(new Integer[] {}));
    verifyNoInteractions(endpointCaller, endpointResponseCache, appEventPublisher);
  }

  @Test
  void aggregateWhenEndpointReturnSuccess() {
    configureEndpointResponse("endpoint1", new int[] {234, 123});

    var actualResult = service.aggregate(List.of("endpoint1"));

    assertThat(actualResult, is(new Integer[] {123, 234}));

    verify(endpointCaller).call(List.of("endpoint1"));
    verifyNoInteractions(endpointResponseCache, appEventPublisher);
  }

  @Test
  void aggregateWhenEndpointReturnError() {
    configureEndpointErrorResponse("endpoint1");

    var actualResult = service.aggregate(List.of("endpoint1"));

    assertThat(actualResult, is(new Integer[] {}));

    verify(endpointCaller).call(List.of("endpoint1"));
    verifyNoInteractions(endpointResponseCache, appEventPublisher);
  }

  @Test
  void aggregateWhenEndpointReturnEmpty() {
    configureEndpointEmptyResponse("endpoint1");

    var actualResult = service.aggregate(List.of("endpoint1"));

    assertThat(actualResult, is(new Integer[] {}));

    verify(endpointCaller).call(List.of("endpoint1"));
    verify(endpointResponseCache).get("endpoint1");
    verify(appEventPublisher).publishEvent(new EmptyResponseEndpointList(List.of("endpoint1")));
  }

  @Test
  void aggregateWhenEndpointReturnEmptyAndCacheHit() {
    configureEndpointEmptyResponse("endpoint1");
    when(endpointResponseCache.get("endpoint1")).thenReturn(Optional.of(new int[] {20, 10}));

    var actualResult = service.aggregate(List.of("endpoint1"));

    assertThat(actualResult, is(new Integer[] {10, 20}));

    verify(endpointCaller).call(List.of("endpoint1"));
    verify(endpointResponseCache).get("endpoint1");
    verify(appEventPublisher).publishEvent(new EmptyResponseEndpointList(List.of("endpoint1")));
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
}