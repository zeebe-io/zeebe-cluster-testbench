package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.ZeebeClientConnectionInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ClientConnectionInfoTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void testDeserialization() throws JsonProcessingException {
    // given
    final String jsonString =
        "{\"ZEEBE_ADDRESS\": \"<zeebeAddress>\",\"ZEEBE_CLIENT_ID\": \"<zeebeClientId>\",\"ZEEBE_AUTHORIZATION_SERVER_URL\": \"<authorizationURL>\"}";

    // when
    final ZeebeClientConnectionInfo actual =
        OBJECT_MAPPER.readValue(jsonString, ZeebeClientConnectionInfo.class);

    // then
    Assertions.assertThat(actual.zeebeAddress()).isEqualTo("<zeebeAddress>");
    Assertions.assertThat(actual.zeebeClientId()).isEqualTo("<zeebeClientId>");
    Assertions.assertThat(actual.zeebeAuthorizationServerUrl()).isEqualTo("<authorizationURL>");
  }
}
