package io.zeebe.clustertestbench.cloud;

import static io.zeebe.clustertestbench.cloud.JsonAssertions.assertJsonEquality;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.CreateClusterRequest;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.CreateZeebeClientRequest;
import org.junit.jupiter.api.Test;

class CloudAPIClientTest {

  @Test
  void testSerializationForCreateClientRequest() throws JsonProcessingException {
    // given
    final var request =
        new CreateClusterRequest(
            "nameValue", "planTypeIdValue", "channelIdValue", "generationIdValue", "regionIdValue");

    final var objectMapper = new ObjectMapper();

    // when
    final var jsonRepresentation = objectMapper.writeValueAsString(request);

    // then
    assertJsonEquality(
        jsonRepresentation,
        """
         {
            "name": "nameValue",
            "planTypeId":"planTypeIdValue",
            "channelId":"channelIdValue",
            "generationId":"generationIdValue",
            "regionId":"regionIdValue"
         }
        """);
  }

  @Test
  void testSerializationForCreateZeebeClientRequest() throws JsonProcessingException {
    // given
    final var request = new CreateZeebeClientRequest("clientNameValue");

    final var objectMapper = new ObjectMapper();

    // when
    final var jsonRepresentation = objectMapper.writeValueAsString(request);

    // then
    assertJsonEquality(
        jsonRepresentation,
        """
          {
             "clientName":"clientNameValue",
             "permissions":["zeebe"]
          }
        """);
  }
}
