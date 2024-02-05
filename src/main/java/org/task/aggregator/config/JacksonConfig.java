package org.task.aggregator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
public class JacksonConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {

    return JsonMapper.builder()
      .disable(INDENT_OUTPUT)
      .disable(WRITE_DATES_AS_TIMESTAMPS)
      .enable(ACCEPT_CASE_INSENSITIVE_ENUMS)
      .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
      .serializationInclusion(NON_NULL)
      .build();
  }
}

