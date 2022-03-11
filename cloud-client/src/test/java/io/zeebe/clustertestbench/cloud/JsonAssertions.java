package io.zeebe.clustertestbench.cloud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;

public class JsonAssertions {

  public static void assertJsonEquality(final String actual, final String expected) {
    final var objectMapper = new ObjectMapper();
    try {
      Assertions.assertThat(objectMapper.readTree(actual))
          .isEqualTo(objectMapper.readTree(expected));
    } catch (final JsonProcessingException e) {
      Assertions.fail(
          "Expected valid JSON, but 'actual' was '%s' and 'expected' was '%s'"
              .formatted(actual, expected));
    }
  }
}
