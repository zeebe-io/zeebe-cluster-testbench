package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ClientConnectiontInfoTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void testDeserialization() throws JsonMappingException, JsonProcessingException {
    // given
    final String jsonString =
        "{\"ZEEBE_ADDRESS\": \"<zeebeAddress>\",\"ZEEBE_CLIENT_ID\": \"<zeebeClientId>\",\"ZEEBE_AUTHORIZATION_SERVER_URL\": \"<authorizationURL>\"}";

    // when
    final ZeebeClientConnectiontInfo actual =
        OBJECT_MAPPER.readValue(jsonString, ZeebeClientConnectiontInfo.class);

    // then
    Assertions.assertThat(actual.getZeebeAddress()).isEqualTo("<zeebeAddress>");
    Assertions.assertThat(actual.getZeebeClientId()).isEqualTo("<zeebeClientId>");
    Assertions.assertThat(actual.getZeebeAuthorizationServerUrl()).isEqualTo("<authorizationURL>");
  }
}
