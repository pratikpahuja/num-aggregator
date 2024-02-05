package org.task.aggregator.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.task.aggregator.config.RestClientConfig;

import java.io.IOException;
import java.net.InetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

class EndpointClientTest {

  MockWebServer server;

  @BeforeEach
  void setup() throws IOException {
    this.server = new MockWebServer();
    this.server.start(InetAddress.getByName("127.0.0.1"), 9999);
  }

  @Test
  void successResponse() throws IOException {
    enqueueResponse();
    var client = createClient();

    callAndAssertResponse(client);
  }

  @Test
  void whenTimeout() throws IOException {
    simulateTimeout();
    var client = createClient();

    callAndAssertException(client, ResponseType.TIMED_OUT);
  }

  @Test
  void whenConnectionFailure() throws IOException {
    simulateConnectionFailure();
    var client = createClient();

    callAndAssertException(client, ResponseType.FAILURE);
  }

  @AfterEach
  void tearDown() throws IOException {
    this.server.shutdown();
  }

  private void callAndAssertResponse(EndpointClient client) throws IOException {
    var response = client.call("http://127.0.0.1:9999");

    assertThat(response.getResponseType(), is(ResponseType.SUCCESS));
    assertThat(response.getNumbers(), is(new int[]{20, 4, 7}));
  }

  private void callAndAssertException(EndpointClient client, ResponseType responseType) throws IOException {
    assertThat(client.call("http://127.0.0.1:9999").getResponseType(), is(responseType));
  }

  private void enqueueResponse() {
    var response = new MockResponse();
    response.setHeader("content-type", "application/json");
    response.setBody("""
      {
        "numbers": [20,4,7]
      }
      """);

    server.enqueue(response);
  }

  private void simulateTimeout() {
    var response = new MockResponse();
    response.setSocketPolicy(SocketPolicy.NO_RESPONSE);

    server.enqueue(response);
  }

  private void simulateConnectionFailure() {
    var response = new MockResponse();
    response.setHttp2ErrorCode(404).setStatus("HTTP/1.1 404");

    server.enqueue(response);
  }

  private EndpointClient createClient() {
    return new EndpointClient(new RestClientConfig(1000).client());
  }

}