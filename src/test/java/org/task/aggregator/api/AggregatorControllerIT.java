package org.task.aggregator.api;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static org.springframework.http.RequestEntity.get;

/**
 * It calls the URLs hosted on wiremock server configured(on port 12345) in docker-compose.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AggregatorControllerIT {

  @Autowired TestRestTemplate restTemplate;

  @Test
  void makeCallWithSuccessfulUrls() throws JSONException {
    var url = "/numbers?"
      + "u=http://localhost:12345/prime"
      + "&u=http://localhost:12345/rand";

    var request = get(url).build();
    var result = restTemplate.exchange(request, String.class);

    assertEquals(expectedResponse(), result.getBody(), LENIENT);
  }

  @Test
  void makeCallWithSuccessfulAndFaultyUrls() throws JSONException {
    var url = "/numbers?"
      + "u=http://localhost:12345/prime"
      + "&u=http://localhost:12345/rand"
      + "&u=http://localhost:12345/fault";

    var request = get(url).build();
    var result = restTemplate.exchange(request, String.class);

    assertEquals(expectedResponse(), result.getBody(), LENIENT);
  }

  private String expectedResponse() {
    return """
      {"numbers":[2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97]}
      """;
  }

}