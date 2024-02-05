package org.task.aggregator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

class EndpointResponseCombinatorTest {

  EndpointResponseCombinator combinator;

  @BeforeEach
  void setup() {
    combinator = new EndpointResponseCombinator();
  }

  @Test
  void checkWithEmptyList() {
    var result = combinator.combine(List.of());
    assertThat(result, is(new int[] {}));
  }

  @Test
  void checkSortingAndDistinct() {
    var numArray1 = new int[] {2, 4, 6, 5, 1, 0};
    var numArray2 = new int[] {10, 3, 4, 1};
    var result = combinator.combine(List.of(numArray1, numArray2));
    assertThat(result, is(new int[] {0, 1, 2, 3, 4, 5, 6, 10}));
  }
}