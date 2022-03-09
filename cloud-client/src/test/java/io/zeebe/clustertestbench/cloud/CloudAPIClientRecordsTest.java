package io.zeebe.clustertestbench.cloud;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.CreateClusterRequest;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.CreateZeebeClientRequest;
import org.junit.jupiter.api.Test;

public class CloudAPIClientRecordsTest {

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
    assertThat(jsonRepresentation)
        .isEqualTo(
            "{\"name\":\"nameValue\",\"planTypeId\":\"planTypeIdValue\",\"channelId\":\"channelIdValue\",\"generationId\":\"generationIdValue\",\"regionId\":\"regionIdValue\"}");
  }

  @Test
  void testSerializationForCreateZeebeClientRequest() throws JsonProcessingException {
    // given
    final var request = new CreateZeebeClientRequest("clientNameValue");

    final var objectMapper = new ObjectMapper();

    // when
    final var jsonRepresentation = objectMapper.writeValueAsString(request);

    // then
    assertThat(jsonRepresentation)
        .isEqualTo("{\"clientName\":\"clientNameValue\",\"permissions\":[\"zeebe\"]}");
  }
}
