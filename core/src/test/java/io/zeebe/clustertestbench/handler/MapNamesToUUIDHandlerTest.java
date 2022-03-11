package io.zeebe.clustertestbench.handler;

import static java.util.Map.entry;
import static org.camunda.community.zeebe.testutils.ZeebeWorkerAssertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.ParametersResponse;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MapNamesToUUIDHandlerTest {

  // An example response from the rest query cluster/parameters with duplicate cluster plan types
  private static final String PARAMETER_RESPONSE_JSON =
      "{"
          + "   \"channels\" : ["
          + "      {"
          + "         \"allowedGenerations\" : ["
          + "           {"
          + "               \"name\" : \"gen1\","
          + "               \"uuid\" : \"uuid-gen1\""
          + "            }"
          + "         ],"
          + "         \"defaultGeneration\" : {"
          + "            \"name\" : \"gen1\","
          + "            \"uuid\" : \"uuid-gen1\""
          + "         },"
          + "         \"name\" : \"channel1\","
          + "         \"uuid\" : \"uuid-channel1\""
          + "      }"
          + "   ],"
          + "   \"clusterPlanTypes\" : ["
          + "      {"
          + "         \"k8sContext\" : {"
          + "            \"name\" : \"region1\","
          + "            \"uuid\" : \"uuid-region1\""
          + "         },"
          + "         \"name\" : \"plan1\","
          + "         \"uuid\" : \"uuid-plan1-region1\""
          + "      },"
          + "      {"
          + "         \"k8sContext\" : {"
          + "            \"name\" : \"region2\","
          + "            \"uuid\" : \"uuid-region2\""
          + "         },"
          + "         \"name\" : \"plan1\","
          + "         \"uuid\" : \"uuid-plan1-region2\""
          + "      },"
          + "      {"
          + "         \"k8sContext\" : {"
          + "            \"name\" : \"region2\","
          + "            \"uuid\" : \"uuid-region2\""
          + "         },"
          + "         \"name\" : \"plan2\","
          + "         \"uuid\" : \"uuid-plan2-region2\""
          + "      }"
          + "   ],"
          + "   \"regions\" : ["
          + "      {"
          + "         \"name\" : \"region1\","
          + "         \"uuid\" : \"uuid-region1\""
          + "      },"
          + "      {"
          + "         \"name\" : \"region2\","
          + "         \"uuid\" : \"uuid-region2\""
          + "      }"
          + "   ]"
          + "}";

  @Mock CloudAPIClient mockCloudAPIClient;

  JobClientStub jobClientStub = new JobClientStub();
  ActivatedJobStub activatedJobStub;

  MapNamesToUUIDsHandler sutHandler;

  @BeforeEach
  void setUp() throws JsonProcessingException {
    sutHandler = new MapNamesToUUIDsHandler(mockCloudAPIClient);
    activatedJobStub = jobClientStub.createActivatedJob();
    final ParametersResponse parameterResponse =
        new ObjectMapper().readValue(PARAMETER_RESPONSE_JSON, ParametersResponse.class);

    when(mockCloudAPIClient.getParameters()).thenReturn(parameterResponse);
  }

  @Test
  void shouldMapNameToUUIDInGivenRegion() throws Exception {
    // given
    activatedJobStub.setInputVariables(
        Map.of(
            "generation",
            "gen1",
            "region",
            "region2",
            "clusterPlan",
            "plan1",
            "channel",
            "channel1"));

    // when
    sutHandler.handle(jobClientStub, activatedJobStub);

    // then
    assertThat(activatedJobStub)
        .completed()
        .extractingOutput()
        .contains(
            entry("clusterPlanUUID", "uuid-plan1-region2"), entry("regionUUID", "uuid-region2"));
  }

  @Test
  void shouldNotMapToClusterPlanInAnotherRegionWhenNotFoundInGivenRegion() {
    // given
    activatedJobStub.setInputVariables(
        Map.of(
            "generation",
            "gen1",
            "region",
            "region1",
            "clusterPlan",
            "plan2",
            "channel",
            "channel1"));

    // when
    Assertions.assertThatThrownBy(() -> sutHandler.handle(jobClientStub, activatedJobStub))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unable to find clusterPlan");
  }
}
