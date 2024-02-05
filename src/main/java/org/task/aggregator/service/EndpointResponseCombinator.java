package org.task.aggregator.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.TreeSet;

@Component
public class EndpointResponseCombinator {

  Integer[] combine(List<int[]> responses) {
    var map = new TreeSet<Integer>();

    for (var response: responses) {
      for (var responseItem: response) {
        map.add(responseItem);
      }
    }

    var resultArr = new Integer[map.size()];

    return map.toArray(resultArr);
  }

}
