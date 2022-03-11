package io.zeebe.clustertestbench.handler;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.community.zeebe.testutils.ZeebeWorkerAssertions.assertThat;
import static org.mockito.Mockito.when;

import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.ClusterInfo;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.GenerationInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckGenerationUsageHandlerTest {

  public static final String GENERATION_FIELD = "generationUUID";
  public static final String RESULT_FIELD = "generationNotInUse";
  private static final String UUID_B = "UUID B";
  private static final String UUID_A = "UUID A";
  private static final GenerationInfo GENERATION_A = new GenerationInfo(UUID_A, "Generation A");
  private static final ClusterInfo CLUSTER_X = createClusterInfoForGeneration(GENERATION_A);
  private static final GenerationInfo GENERATION_B = new GenerationInfo(UUID_B, "Generation B");
  private static final ClusterInfo CLUSTER_Z = createClusterInfoForGeneration(GENERATION_B);
  private static final ClusterInfo CLUSTER_Y = createClusterInfoForGeneration(GENERATION_B);
  private static final List<ClusterInfo> CLUSTERS = List.of(CLUSTER_Z, CLUSTER_Y, CLUSTER_X);

  @Mock CloudAPIClient mockCloudAPIClient;

  JobClientStub jobClientStub = new JobClientStub();
  ActivatedJobStub activatedJobStub;

  CheckGenerationUsageHandler sutHandler;

  @BeforeEach
  void setUp() {
    sutHandler = new CheckGenerationUsageHandler(mockCloudAPIClient);
    activatedJobStub = jobClientStub.createActivatedJob();
  }

  @Test
  void shouldReturnFalseWhenGenerationIsInUse() throws Exception {
    // given
    activatedJobStub.setInputVariables(Map.of(GENERATION_FIELD, UUID_A));

    when(mockCloudAPIClient.listClusterInfos()).thenReturn(CLUSTERS);

    // when
    sutHandler.handle(jobClientStub, activatedJobStub);

    // then
    assertThat(activatedJobStub)
        .completed()
        .extractingOutput()
        .contains(entry(RESULT_FIELD, false));
  }

  @Test
  void shouldReturnTrueWhenGenerationIsNotUsed() throws Exception {
    // given
    activatedJobStub.setInputVariables(Map.of(GENERATION_FIELD, "UUID_C"));

    when(mockCloudAPIClient.listClusterInfos()).thenReturn(CLUSTERS);

    // when
    sutHandler.handle(jobClientStub, activatedJobStub);

    // then
    assertThat(activatedJobStub).completed().extractingOutput().contains(entry(RESULT_FIELD, true));
  }

  @Test
  void shouldReturnTrueWhenThereAreNoClustersAtAll() throws Exception {
    // given
    activatedJobStub.setInputVariables(Map.of(GENERATION_FIELD, UUID_A));

    when(mockCloudAPIClient.listClusterInfos()).thenReturn(Collections.emptyList());

    // when
    sutHandler.handle(jobClientStub, activatedJobStub);

    // then
    assertThat(activatedJobStub).completed().extractingOutput().contains(entry(RESULT_FIELD, true));
  }

  @Test
  void shouldThrowExceptionIfGenerationUUIDIsNull() {
    // given
    final var input = new HashMap<String, Object>();
    input.put(GENERATION_FIELD, null);

    activatedJobStub.setInputVariables(input);

    // when
    assertThatThrownBy(() -> sutHandler.handle(jobClientStub, activatedJobStub))
        .isExactlyInstanceOf(IllegalArgumentException.class);

    assertThat(activatedJobStub).isStillActivated();
  }

  private static ClusterInfo createClusterInfoForGeneration(final GenerationInfo generation) {
    return new ClusterInfo(null, null, null, null, null, generation, null, null, null);
  }
}
