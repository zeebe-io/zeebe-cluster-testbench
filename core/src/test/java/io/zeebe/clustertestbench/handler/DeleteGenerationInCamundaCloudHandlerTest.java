package io.zeebe.clustertestbench.handler;

import static org.camunda.community.zeebe.testutils.ZeebeWorkerAssertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.zeebe.clustertestbench.internal.cloud.ExternalConsoleAPIClient;
import java.util.Map;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteGenerationInCamundaCloudHandlerTest {

  private static final String TEST_GENERATION_UUID = "test-generation-uuid";

  @Mock ExternalConsoleAPIClient mockInternalApiClient;

  JobClientStub jobClientStub = new JobClientStub();

  ActivatedJobStub activatedJobStub;

  DeleteGenerationInCamundaCloudHandler sutDeleteGenerationHandler;

  @BeforeEach
  public void setUp() {
    sutDeleteGenerationHandler = new DeleteGenerationInCamundaCloudHandler(mockInternalApiClient);

    activatedJobStub = jobClientStub.createActivatedJob();

    activatedJobStub.setInputVariables(Map.of("generationUUID", TEST_GENERATION_UUID));
  }

  @Test
  void shouldCallApiToDeleteGeneration() throws Exception {
    // when
    sutDeleteGenerationHandler.handle(jobClientStub, activatedJobStub);

    // then
    verify(mockInternalApiClient).deleteGeneration(Mockito.any());
    verifyNoMoreInteractions(mockInternalApiClient);
  }

  @Test
  void shouldDeleteTheRightGeneration() throws Exception {
    // when
    sutDeleteGenerationHandler.handle(jobClientStub, activatedJobStub);

    // then
    verify(mockInternalApiClient).deleteGeneration(TEST_GENERATION_UUID);
    verifyNoMoreInteractions(mockInternalApiClient);
  }

  @Test
  void shouldCompleteJobAfterDeletingTheGeneration() throws Exception {
    final var spyJobClientStub = spy(jobClientStub);
    // when
    sutDeleteGenerationHandler.handle(spyJobClientStub, activatedJobStub);

    // then
    final var inOrder = inOrder(mockInternalApiClient, spyJobClientStub);

    inOrder.verify(mockInternalApiClient).deleteGeneration(Mockito.any());
    inOrder.verify(spyJobClientStub).newCompleteCommand(Mockito.anyLong());

    verifyNoMoreInteractions(mockInternalApiClient);
    verifyNoMoreInteractions(spyJobClientStub);

    assertThat(activatedJobStub).completed();
  }
}
