package org.task.aggregator.service;

import lombok.Data;

import java.util.List;

@Data
public class EmptyResponseEndpointList {
  private final List<String> urls;
}
