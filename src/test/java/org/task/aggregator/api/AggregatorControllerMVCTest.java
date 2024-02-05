package org.task.aggregator.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.task.aggregator.service.AggregatorService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.nCopies;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AggregatorController.class)
class AggregatorControllerMVCTest {

  @Autowired MockMvc mvc;
  @MockBean AggregatorService service;

  @ParameterizedTest
  @ValueSource(strings = {
    "asd",
    "http1://127.0.0.1:8090/primes"
  })
  void onlyInvalidUrls(String url) throws Exception {
    performActions(url)
      .andExpect(status().isOk());

    verify(service).aggregate(List.of());
  }

  @Test
  void moreThanAllowedEndpoints() throws Exception {
    var urlParams = nCopies(11, "http://127.0.0.1:8090/primes");

    performActions(urlParams.toArray(new String[11]))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  void mixOfValidAndInvalid() throws Exception {
    when(service.aggregate(List.of("http://127.0.0.1:8090/primes", "http://127.0.0.1:8090/fibo")))
      .thenReturn(new Integer[] {123, 234});

    var result = performActions("url", "http://127.0.0.1:8090/primes", "http://127.0.0.1:8090/fibo", "123")
      .andExpect(status().isOk())
      .andReturn();

    verify(service).aggregate(List.of("http://127.0.0.1:8090/primes", "http://127.0.0.1:8090/fibo"));
    var responseBody = result.getResponse().getContentAsString();

    var expectedResponseBody = """
      {
        "numbers": [123, 234]
      }
      """;
    JSONAssert.assertEquals(expectedResponseBody, responseBody, JSONCompareMode.LENIENT);
  }

  ResultActions performActions(String... urls) throws Exception {
    var request = get("/numbers");

    for (var url: urls)
      request.param("u", url);

    return mvc.perform(request);
  }
}