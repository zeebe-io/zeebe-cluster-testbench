package io.zeebe.clustertestbench.handler;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.zeebe.clustertestbench.handler.DeleteGenerationInCamundaCloudHandler.Input;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteGenerationInCamundaCloudHandlerTest {

  private static final String TEST_GENERATION_UUID = "test-generation-uuid";

  @Mock InternalCloudAPIClient mockInternalApiClient;

  @Mock JobClient mockJobClient;

  @Mock CompleteJobCommandStep1 mockCompleteJobCommandStep1;

  @SuppressWarnings("rawtypes")
  @Mock
  ZeebeFuture mockZeebeFuture;

  @Mock ActivatedJob mockActivatedJob;

  DeleteGenerationInCamundaCloudHandler sutDeleteGenerationhandler;

  @SuppressWarnings("unchecked")
  @BeforeEach
  public void setUp() {
    sutDeleteGenerationhandler = new DeleteGenerationInCamundaCloudHandler(mockInternalApiClient);
    when(mockJobClient.newCompleteCommand(Mockito.anyLong()))
        .thenReturn(mockCompleteJobCommandStep1);
    when(mockCompleteJobCommandStep1.send()).thenReturn(mockZeebeFuture);

    final var input = new Input();
    input.setGenerationUUID(TEST_GENERATION_UUID);

    when(mockActivatedJob.getVariablesAsType(Input.class)).thenReturn(input);
  }

  @Test
  void shouldCallApiToDeleteGeneration() throws Exception {
    // when
    sutDeleteGenerationhandler.handle(mockJobClient, mockActivatedJob);

    // then
    verify(mockInternalApiClient).deleteGeneration(Mockito.any());
    verifyNoMoreInteractions(mockInternalApiClient);
  }

  @Test
  void shouldDeleteTheRightGeneration() throws Exception {
    // when
    sutDeleteGenerationhandler.handle(mockJobClient, mockActivatedJob);

    // then
    verify(mockInternalApiClient).deleteGeneration(TEST_GENERATION_UUID);
    verifyNoMoreInteractions(mockInternalApiClient);
  }

  @Test
  void shouldCompleteJob() throws Exception {
    // when
    sutDeleteGenerationhandler.handle(mockJobClient, mockActivatedJob);

    // then
    verify(mockJobClient).newCompleteCommand(Mockito.anyLong());
    verify(mockCompleteJobCommandStep1).send();
    verify(mockZeebeFuture).join();

    verifyNoMoreInteractions(mockJobClient);
    verifyNoMoreInteractions(mockCompleteJobCommandStep1);
    verifyNoMoreInteractions(mockZeebeFuture);
  }

  @Test
  void shouldCompleteJobAfterDeletingTheGeneration() throws Exception {
    // when
    sutDeleteGenerationhandler.handle(mockJobClient, mockActivatedJob);

    // then
    final var inOrder = inOrder(mockInternalApiClient, mockJobClient);

    inOrder.verify(mockInternalApiClient).deleteGeneration(Mockito.any());
    inOrder.verify(mockJobClient).newCompleteCommand(Mockito.anyLong());

    verifyNoMoreInteractions(mockInternalApiClient);
    verifyNoMoreInteractions(mockJobClient);
  }

  @Test
  void shouldCompleteTheRightJob() throws Exception {
    // given
    final var jobKey = 42L;
    when(mockActivatedJob.getKey()).thenReturn(jobKey);

    // when
    sutDeleteGenerationhandler.handle(mockJobClient, mockActivatedJob);

    // then
    verify(mockJobClient).newCompleteCommand(jobKey);
  }
}
