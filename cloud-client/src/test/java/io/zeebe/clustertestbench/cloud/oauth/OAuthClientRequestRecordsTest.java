package io.zeebe.clustertestbench.cloud.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.clustertestbench.cloud.oauth.OAuthClient.ServiceAccountTokenRequest;
import io.zeebe.clustertestbench.cloud.oauth.OAuthClient.UserAccountTokenRequest;
import org.junit.jupiter.api.Test;

class OAuthClientRequestRecordsTest {

  @Test
  void testSerializationForServiceAccountTokenRequest() throws JsonProcessingException {
    // given
    final var request =
        new ServiceAccountTokenRequest("audience", "clientId", "clientSecret", "grantType");

    final var objectMapper = new ObjectMapper();

    // when
    final var jsonRepresentation = objectMapper.writeValueAsString(request);

    // then
    assertThat(jsonRepresentation)
        .isEqualTo(
            "{\"client_id\":\"clientId\",\"client_secret\":\"clientSecret\",\"grant_type\":\"grantType\",\"audience\":\"audience\"}");
  }

  @Test
  void testSerializationForUserAccountTokenRequest() throws JsonProcessingException {
    // given
    final var request =
        new UserAccountTokenRequest(
            "audience", "clientId", "clientSecret", "grantType", "username", "password");

    final var objectMapper = new ObjectMapper();

    // when
    final var jsonRepresentation = objectMapper.writeValueAsString(request);

    // then
    assertThat(jsonRepresentation)
        .isEqualTo(
            "{\"client_id\":\"clientId\",\"client_secret\":\"clientSecret\",\"grant_type\":\"grantType\",\"audience\":\"audience\",\"username\":\"username\",\"password\":\"password\"}");
  }
}
