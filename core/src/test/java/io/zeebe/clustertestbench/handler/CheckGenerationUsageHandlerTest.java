package io.zeebe.clustertestbench.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.response.ClusterInfo;
import io.zeebe.clustertestbench.cloud.response.GenerationInfo;
import io.zeebe.clustertestbench.handler.CheckGenerationUsageHandler.Input;
import io.zeebe.clustertestbench.handler.CheckGenerationUsageHandler.Output;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckGenerationUsageHandlerTest {
  private static final Long TEST_JOB_KEY = 42L;

  private static final String UUID_B = "UUID B";
  private static final String UUID_A = "UUID A";
  private static final GenerationInfo GENERATION_A = new GenerationInfo();
  private static final GenerationInfo GENERATION_B = new GenerationInfo();
  private static final ClusterInfo CLUSTER_Z = new ClusterInfo();
  private static final ClusterInfo CLUSTER_Y = new ClusterInfo();
  private static final ClusterInfo CLUSTER_X = new ClusterInfo();
  private static final List<ClusterInfo> CLUSTERS = List.of(CLUSTER_Z, CLUSTER_Y, CLUSTER_X);

  static {
    GENERATION_A.setName("Generation A");
    GENERATION_A.setUuid(UUID_A);
    GENERATION_B.setName("Generation B");
    GENERATION_B.setUuid(UUID_B);

    CLUSTER_Z.setGeneration(GENERATION_B);
    CLUSTER_Y.setGeneration(GENERATION_B);
    CLUSTER_X.setGeneration(GENERATION_A);
  }

  @Mock CloudAPIClient mockCloudAPIClient;

  @Mock JobClient mockJobClient;

  @Mock CompleteJobCommandStep1 mockCompleteJobCommandStep1;
  @Mock CompleteJobCommandStep1 mockCompleteJobCommandStep2;

  @SuppressWarnings("rawtypes")
  @Mock
  ZeebeFuture mockZeebeFuture;

  @Mock ActivatedJob mockActivatedJob;

  CheckGenerationUsageHandler sutHandler;

  @BeforeEach
  void setUp() {
    sutHandler = new CheckGenerationUsageHandler(mockCloudAPIClient);
  }

  @Test
  void shouldReturnFalseWhenGeenrationIsInUse() throws Exception {
    // given
    final var input = new Input();
    input.setGenerationUUID(UUID_A);

    when(mockActivatedJob.getVariablesAsType(Input.class)).thenReturn(input);

    when(mockCloudAPIClient.listClusterInfos()).thenReturn(CLUSTERS);

    mockJobCompletChain();

    // when
    sutHandler.handle(mockJobClient, mockActivatedJob);

    // then
    verify(mockJobClient).newCompleteCommand(TEST_JOB_KEY);

    final var argumentCapture = ArgumentCaptor.forClass(Output.class);
    verify(mockCompleteJobCommandStep1).variables(argumentCapture.capture());

    final var output = argumentCapture.getValue();

    assertThat(output.isGenerationNotInUse()).isFalse();

    verify(mockCompleteJobCommandStep2).send();
    verify(mockZeebeFuture).join();
  }

  @Test
  void shouldReturnTrueWhenGeenrationIsNotUsed() throws Exception {
    // given
    final var input = new Input();
    input.setGenerationUUID("UUID C");

    when(mockActivatedJob.getVariablesAsType(Input.class)).thenReturn(input);

    when(mockCloudAPIClient.listClusterInfos()).thenReturn(CLUSTERS);

    mockJobCompletChain();

    // when
    sutHandler.handle(mockJobClient, mockActivatedJob);

    // then
    verify(mockJobClient).newCompleteCommand(TEST_JOB_KEY);

    final var argumentCapture = ArgumentCaptor.forClass(Output.class);
    verify(mockCompleteJobCommandStep1).variables(argumentCapture.capture());

    final var output = argumentCapture.getValue();

    assertThat(output.isGenerationNotInUse()).isTrue();

    verify(mockCompleteJobCommandStep2).send();
    verify(mockZeebeFuture).join();
  }

  @Test
  void shouldReturnTrueWhenThereAreNoClustersAtAll() throws Exception {
    // given
    final var input = new Input();
    input.setGenerationUUID(UUID_A);

    when(mockActivatedJob.getVariablesAsType(Input.class)).thenReturn(input);

    when(mockCloudAPIClient.listClusterInfos()).thenReturn(Collections.emptyList());

    mockJobCompletChain();

    // when
    sutHandler.handle(mockJobClient, mockActivatedJob);

    // then
    verify(mockJobClient).newCompleteCommand(TEST_JOB_KEY);

    final var argumentCapture = ArgumentCaptor.forClass(Output.class);
    verify(mockCompleteJobCommandStep1).variables(argumentCapture.capture());

    final var output = argumentCapture.getValue();

    assertThat(output.isGenerationNotInUse()).isTrue();

    verify(mockCompleteJobCommandStep2).send();
    verify(mockZeebeFuture).join();
  }

  @Test
  void shouldThrowExceptionIfGenerationUUIDIsNull() {
    // given
    final var input = new Input();
    input.setGenerationUUID(null);

    when(mockActivatedJob.getVariablesAsType(Input.class)).thenReturn(input);

    // when
    Assertions.assertThatThrownBy(() -> sutHandler.handle(mockJobClient, mockActivatedJob))
        .isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @SuppressWarnings("unchecked")
  private void mockJobCompletChain() {
    when(mockActivatedJob.getKey()).thenReturn(TEST_JOB_KEY);
    when(mockJobClient.newCompleteCommand(Mockito.anyLong()))
        .thenReturn(mockCompleteJobCommandStep1);
    when(mockCompleteJobCommandStep1.variables((Object) Mockito.any()))
        .thenReturn(mockCompleteJobCommandStep2);
    when(mockCompleteJobCommandStep2.send()).thenReturn(mockZeebeFuture);
  }
}
