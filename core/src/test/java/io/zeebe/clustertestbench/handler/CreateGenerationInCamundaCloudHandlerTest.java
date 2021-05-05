package io.zeebe.clustertestbench.handler;

import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_CHANNEL_NAME;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_CHANNEL_UUID;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_ELASTIC_CURATOR_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_ELASTIC_OSS_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_GENERATION_NAME;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_OPERATE_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.KEY_ELASTIC_CURATOR_IMAGEE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.KEY_ELASTIC_OSS_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.KEY_OPERATE_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.KEY_ZEEBE_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.zeebe.clustertestbench.cloud.response.GenerationInfo;
import io.zeebe.clustertestbench.handler.CreateGenerationInCamundaCloudHandler.Input;
import io.zeebe.clustertestbench.handler.CreateGenerationInCamundaCloudHandler.Output;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient;
import io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient;
import io.zeebe.clustertestbench.internal.cloud.request.CreateGenerationRequest;
import io.zeebe.clustertestbench.internal.cloud.request.UpdateChannelRequest;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateGenerationInCamundaCloudHandlerTest {
  private static final String ZEEBE_IMAGE = "testImage";

  @Nested
  @DisplayName("Handle Job")
  class HandleJobTest {

    final InternalCloudAPIClient stubInternalApiClient = new StubInternalCloudAPIClient(false);

    InternalCloudAPIClient spyInternalApiClient;

    CreateGenerationInCamundaCloudHandler sutCreateGenerationHandler;

    @Mock JobClient mockJobClient;

    @Mock CompleteJobCommandStep1 mockCompleteJobCommandStep1;
    @Mock CompleteJobCommandStep1 mockCompleteJobCommandStep2;

    @SuppressWarnings("rawtypes")
    @Mock
    ZeebeFuture mockZeebeFuture;

    @Mock ActivatedJob mockActivatedJob;

    @BeforeEach
    public void setUp() {
      spyInternalApiClient = spy(stubInternalApiClient);
      sutCreateGenerationHandler = new CreateGenerationInCamundaCloudHandler(spyInternalApiClient);

      final var input = new Input();
      input.setZeebeImage(ZEEBE_IMAGE);
      input.setGenerationTemplate(DEFAULT_GENERATION_NAME);
      input.setChannel(DEFAULT_CHANNEL_NAME);

      when(mockActivatedJob.getVariablesAsType(Input.class)).thenReturn(input);
    }

    @Test
    public void shouldThrowRuntimExceptionIfGenerationTemplateCannotBeFound() throws Exception {
      // given
      final var input = new Input();
      input.setZeebeImage(ZEEBE_IMAGE);
      input.setGenerationTemplate("unknown-generation");

      when(mockActivatedJob.getVariablesAsType(Input.class)).thenReturn(input);

      // when + then
      assertThatThrownBy(() -> sutCreateGenerationHandler.handle(mockJobClient, mockActivatedJob))
          .isExactlyInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unable to find generation")
          .hasMessageContaining("unknown-generation");
    }

    @Test
    public void shouldThrowRuntimExceptionIfChannelCannotBeFound() throws Exception {
      // given
      final var input = new Input();
      input.setGenerationTemplate(DEFAULT_GENERATION_NAME);
      input.setChannel("unknown-channel");

      when(mockActivatedJob.getVariablesAsType(Input.class)).thenReturn(input);

      // when + then
      assertThatThrownBy(() -> sutCreateGenerationHandler.handle(mockJobClient, mockActivatedJob))
          .isExactlyInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unable to find channel")
          .hasMessageContaining("unknown-channel");
    }

    @Test
    public void shouldCreateGeneration() throws Exception {
      // given
      mockJobCompletChain();

      // when
      sutCreateGenerationHandler.handle(mockJobClient, mockActivatedJob);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(CreateGenerationRequest.class);
      verify(spyInternalApiClient).createGeneration(argumentCapture.capture());
      final var request = argumentCapture.getValue();

      assertThat(request.getVersions())
          .containsOnly(
              entry(KEY_ZEEBE_IMAGE, ZEEBE_IMAGE),
              entry(KEY_OPERATE_IMAGE, DEFAULT_OPERATE_IMAGE),
              entry(KEY_ELASTIC_CURATOR_IMAGEE, DEFAULT_ELASTIC_CURATOR_IMAGE),
              entry(KEY_ELASTIC_OSS_IMAGE, DEFAULT_ELASTIC_OSS_IMAGE));
    }

    @Test
    public void shouldThrowExceptiionIfGenerationWasNotCreated() throws Exception {
      // given
      final var brokenStubInternalApiClient = new StubInternalCloudAPIClient(true);
      final var spyStubInternalApiClient = spy(brokenStubInternalApiClient);

      final var sutLocal = new CreateGenerationInCamundaCloudHandler(spyStubInternalApiClient);

      // when + then
      assertThatThrownBy(() -> sutLocal.handle(mockJobClient, mockActivatedJob))
          .isExactlyInstanceOf(RuntimeException.class)
          .hasMessageContaining("Creation of generation unsuccessful");
    }

    @Test
    public void shouldAddGenerationToChannel() throws Exception {
      // given
      mockJobCompletChain();

      // when
      sutCreateGenerationHandler.handle(mockJobClient, mockActivatedJob);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(UpdateChannelRequest.class);
      verify(spyInternalApiClient)
          .updateChannel(Mockito.eq(DEFAULT_CHANNEL_UUID), argumentCapture.capture());
      final var request = argumentCapture.getValue();

      final var generationUUIDs =
          stubInternalApiClient.listGenerationInfos().stream()
              .map(GenerationInfo::getUuid)
              .collect(Collectors.toList());

      assertThat(request.getName()).isEqualTo(StubInternalCloudAPIClient.DEFAULT_CHANNEL_NAME);
      assertThat(request.getAllowedGenerationIds()).containsExactlyElementsOf(generationUUIDs);
    }

    @Test
    void shouldCompleteJob() throws Exception {
      // given
      mockJobCompletChain();

      // when
      sutCreateGenerationHandler.handle(mockJobClient, mockActivatedJob);

      // then
      verify(mockJobClient).newCompleteCommand(Mockito.anyLong());
      verify(mockCompleteJobCommandStep1).variables((Object) Mockito.any());
      verify(mockCompleteJobCommandStep2).send();
      verify(mockZeebeFuture).join();

      verifyNoMoreInteractions(mockJobClient);
      verifyNoMoreInteractions(mockCompleteJobCommandStep1);
      verifyNoMoreInteractions(mockCompleteJobCommandStep2);
      verifyNoMoreInteractions(mockZeebeFuture);
    }

    @Test
    void shouldCompleteTheRightJob() throws Exception {
      // given
      final var jobKey = 42L;
      when(mockActivatedJob.getKey()).thenReturn(jobKey);
      mockJobCompletChain();

      // when
      sutCreateGenerationHandler.handle(mockJobClient, mockActivatedJob);

      // then
      verify(mockJobClient).newCompleteCommand(jobKey);
    }

    @Test
    void shouldSetGenerationNameAndUuidUponCompletion() throws Exception {
      // given
      mockJobCompletChain();
      final var initialGenerations = new ArrayList<>(stubInternalApiClient.listGenerationInfos());

      // when
      sutCreateGenerationHandler.handle(mockJobClient, mockActivatedJob);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(Output.class);
      verify(mockCompleteJobCommandStep1).variables(argumentCapture.capture());

      final var output = argumentCapture.getValue();

      final var createdGenerations = new ArrayList<>(stubInternalApiClient.listGenerationInfos());
      createdGenerations.removeAll(initialGenerations);

      assertThat(createdGenerations).hasSize(1);

      final var createdGeneration = createdGenerations.get(0);

      assertThat(output.getGeneration()).isEqualTo(createdGeneration.getName());
      assertThat(output.getGenerationUUID()).isEqualTo(createdGeneration.getUuid());
    }

    @Test
    void shouldDeleteGenerationIfExceptionOccursAfterGenerationHasBennCreated() throws Exception {
      // given
      final var message =
          "Deliberate runtime exception in shouldDeleteGenerationIfExceptionOccursAfterGenerationHasBennCreated";
      doThrow(new RuntimeException(message))
          .when(spyInternalApiClient)
          .updateChannel(Mockito.any(), Mockito.any());

      // when + then
      assertThatThrownBy(() -> sutCreateGenerationHandler.handle(mockJobClient, mockActivatedJob))
          .isExactlyInstanceOf(RuntimeException.class)
          .hasMessage(message);

      verify(spyInternalApiClient).deleteGeneration(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    private void mockJobCompletChain() {
      when(mockJobClient.newCompleteCommand(Mockito.anyLong()))
          .thenReturn(mockCompleteJobCommandStep1);
      when(mockCompleteJobCommandStep1.variables((Object) Mockito.any()))
          .thenReturn(mockCompleteJobCommandStep2);
      when(mockCompleteJobCommandStep2.send()).thenReturn(mockZeebeFuture);
    }
  }

  @Nested
  @DisplayName("Generation Name")
  class GenerationNameTest {

    @Test
    void shouldStartWithTemp() {
      // when
      final var actual = CreateGenerationInCamundaCloudHandler.createGenerationName();

      // then
      assertThat(actual).startsWith("temp");
    }

    @Test
    void shouldGenerateDifferentNamesWhenCalledMultipletimes() {
      // when
      final var actual1 = CreateGenerationInCamundaCloudHandler.createGenerationName();
      final var actual2 = CreateGenerationInCamundaCloudHandler.createGenerationName();

      // then
      assertThat(actual1).isNotEqualTo(actual2);
    }
  }
}
