package org.task.aggregator.client;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static org.task.aggregator.client.ResponseType.*;

/**
 * Make sure to call isSuccess() before trying to retrieve numbers.
 */
@Getter
@NoArgsConstructor
@Data
public class EndpointResponse {
  private ResponseType responseType;
  private int[] numbers;

  public EndpointResponse(int[] numbers) {
    responseType = SUCCESS;
    this.numbers = numbers;
  }

  void setNumbers(int[] numbers) {
    responseType = SUCCESS;
    this.numbers = numbers;
  }

  public EndpointResponse(ResponseType responseType) {
    this.responseType = responseType;
  }

  public static EndpointResponse errorResponse() {
    return new EndpointResponse(FAILURE);
  }

  public static EndpointResponse timeOutResponse() {
    return new EndpointResponse(TIMED_OUT);
  }

  public boolean isSuccess() {
    return responseType == SUCCESS;
  }

  public boolean isFailure() {
    return !isSuccess();
  }

  public boolean isTimedOut() {
    return responseType == TIMED_OUT;
  }
}
