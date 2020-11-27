package io.zeebe.clustertestbench.cloud.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OAuthCredentialsTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void testDeserialization() throws JsonMappingException, JsonProcessingException {
    // given
    final String exampleResponse =
        "{\r\n"
            + "    \"access_token\": \"lorem ipsum\",\r\n"
            + "    \"expires_in\": 86400,\r\n"
            + "    \"token_type\": \"Bearer\"\r\n"
            + "}";

    // when
    final OAuthCredentials actual =
        OBJECT_MAPPER.readValue(exampleResponse, OAuthCredentials.class);

    // then
    assertThat(actual.getTokenType()).isEqualTo("Bearer");
    assertThat(actual.getAccessToken()).isEqualTo("lorem ipsum");
    assertThat(actual.getExpiry())
        .isCloseTo(
            ZonedDateTime.now().plusSeconds(86400), Assertions.within(1, ChronoUnit.SECONDS));
  }
}
