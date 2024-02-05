package org.task.aggregator.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.task.aggregator.interfaces.AggregatorResponse;
import org.task.aggregator.service.AggregatorService;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController()
@RequestMapping("/numbers")
@RequiredArgsConstructor
@Slf4j
public class AggregatorController {

  private final AggregatorService service;

  @GetMapping
  public AggregatorResponse aggregate(@RequestParam("u") List<String> endpoints) {
    if (endpoints.size() > 10) {
      throw new BadRequestException("More than allowed endpoints supplied.");
    }

    var validEndpoints = filterValidEndpoints(endpoints);
    return new AggregatorResponse(service.aggregate(validEndpoints));
  }

  private List<String> filterValidEndpoints(List<String> endpoints) {
    return endpoints.stream()
      .filter(this::isValidUrl)
      .toList();
  }

  boolean isValidUrl(String url) {
    try {
      new URI(url).toURL();

      return true;
    } catch (MalformedURLException | URISyntaxException| IllegalArgumentException e) {
      log.error(STR."Invalid URL supplied: \{url}");
      return false;
    }
  }

}

class BadRequestException extends ResponseStatusException {
  public BadRequestException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
