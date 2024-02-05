package org.task.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.task.aggregator.caller.EndpointCaller;
import org.task.aggregator.client.EndpointResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AggregatorService {

  private final EndpointCaller caller;
  private final EndpointResponseCache cache;
  private final ApplicationEventPublisher publisher;
  private final EndpointResponseCombinator combinator;

  public Integer[] aggregate(List<String> endpoints) {
    if (endpoints.isEmpty())
      return new Integer[] {};

    var responses = callEndpoints(endpoints);
    var numbersArray = getNumbersFromResponses(responses);

    var timedOutEndpoints = getTimedOutEndpoints(responses);

    var cachedNumbersArray = getNumbersFromCache(timedOutEndpoints);

    retryEndpoints(timedOutEndpoints);

    numbersArray.addAll(cachedNumbersArray);
    return combinator.combine(numbersArray);
  }

  private List<int[]> getNumbersFromCache(List<String> emptyResponseEndpoints) {
    if (emptyResponseEndpoints.isEmpty())
      return List.of();

    return emptyResponseEndpoints.stream()
    .map(cache::get)
    .filter(Optional::isPresent)
    .map(Optional::get)
    .toList();
  }

  private Map<String, Optional<EndpointResponse>> callEndpoints(List<String> endpoints) {
    return caller.call(endpoints);
  }

  private static List<int[]> getNumbersFromResponses(Map<String, Optional<EndpointResponse>> responses) {
    var numbersArrayList = new ArrayList<int[]>();

    responses.values().stream()
      .filter(Optional::isPresent)
      .map(Optional::get)
      .filter(EndpointResponse::isSuccess)
      .map(EndpointResponse::getNumbers)
      .forEach(numbersArrayList::add);

    return numbersArrayList;
  }

  private static List<String> getTimedOutEndpoints(Map<String, Optional<EndpointResponse>> responses) {
    return responses.entrySet().stream()
      .filter(response -> response.getValue().isEmpty() || response.getValue().get().isTimedOut())
      .map(Map.Entry::getKey)
      .toList();
  }

  private void retryEndpoints(List<String> endpoints) {
    if (endpoints.isEmpty())
      return;

    publisher.publishEvent(new EmptyResponseEndpointList(endpoints));
  }
}
