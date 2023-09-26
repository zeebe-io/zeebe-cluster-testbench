package io.zeebe.clustertestbench.handler;

import static io.zeebe.clustertestbench.internal.cloud.StubExternalConsoleAPIClient.DEFAULT_GENERATION_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.camunda.community.zeebe.testutils.ZeebeWorkerAssertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.zeebe.clustertestbench.internal.cloud.ExternalConsoleAPIClient;
import io.zeebe.clustertestbench.internal.cloud.ExternalConsoleAPIClient.CloneGenerationRequest;
import io.zeebe.clustertestbench.internal.cloud.StubExternalConsoleAPIClient;
import java.util.ArrayList;
import java.util.Map;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateGenerationInCamundaCloudHandlerTest {
  private static final String ZEEBE_IMAGE = "zeebeTestImage";
  private static final String OPERATE_IMAGE = "operateTestImage";

  @Nested
  @DisplayName("Handle Job")
  class HandleJobTest {

    public static final String FIELD_ZEEBE_IMAGE = "zeebeImage";
    public static final String FIELD_OPERATE_IMAGE = "operateImage";

    public static final String FIELD_GENERATION_TEMPLATE = "generationTemplate";
    final ExternalConsoleAPIClient stubExternalConsoleAPIClient =
        new StubExternalConsoleAPIClient(false);

    ExternalConsoleAPIClient spyInternalApiClient;

    CreateGenerationInCamundaCloudHandler sutCreateGenerationHandler;

    JobClientStub jobClientStub = new JobClientStub();

    ActivatedJobStub activatedJobStub;

    @BeforeEach
    public void setUp() {
      spyInternalApiClient = spy(stubExternalConsoleAPIClient);
      sutCreateGenerationHandler = new CreateGenerationInCamundaCloudHandler(spyInternalApiClient);

      activatedJobStub = jobClientStub.createActivatedJob();

      activatedJobStub.setInputVariables(
          Map.of(
              FIELD_ZEEBE_IMAGE, ZEEBE_IMAGE, FIELD_GENERATION_TEMPLATE, DEFAULT_GENERATION_NAME));
    }

    @Test
    public void shouldThrowRuntimeExceptionIfGenerationTemplateCannotBeFound() {
      // given
      activatedJobStub.setInputVariables(
          Map.of(FIELD_ZEEBE_IMAGE, ZEEBE_IMAGE, FIELD_GENERATION_TEMPLATE, "unknown-generation"));

      // when + then
      assertThatThrownBy(() -> sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub))
          .isExactlyInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unable to find generation")
          .hasMessageContaining("unknown-generation");

      assertThat(activatedJobStub).isStillActivated();
    }

    @Test
    public void shouldCreateGenerationWithDefaultVersions() throws Exception {
      // given
      activatedJobStub.setInputVariables(
          Map.of(FIELD_GENERATION_TEMPLATE, DEFAULT_GENERATION_NAME));

      // when
      sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(CloneGenerationRequest.class);
      final var stringCapture = ArgumentCaptor.forClass(String.class);
      verify(spyInternalApiClient)
          .cloneGeneration(stringCapture.capture(), argumentCapture.capture());
      final var request = argumentCapture.getValue();

      assertThat(request.operateVersion()).isNull();
      assertThat(request.zeebeVersion()).isNull();
    }

    @Test
    public void shouldCreateGenerationWithSpecificZeebeVersion() throws Exception {
      // when
      sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(CloneGenerationRequest.class);
      final var stringCapture = ArgumentCaptor.forClass(String.class);
      verify(spyInternalApiClient)
          .cloneGeneration(stringCapture.capture(), argumentCapture.capture());
      final var request = argumentCapture.getValue();

      assertThat(request.operateVersion()).isNull();
      assertThat(request.zeebeVersion()).isEqualTo(ZEEBE_IMAGE);
    }

    @Test
    public void shouldCreateGenerationWithSpecificAppVersions() throws Exception {
      // given
      activatedJobStub.setInputVariables(
          Map.of(
              FIELD_OPERATE_IMAGE,
              OPERATE_IMAGE,
              FIELD_GENERATION_TEMPLATE,
              DEFAULT_GENERATION_NAME));

      // when
      sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(CloneGenerationRequest.class);
      final var stringCapture = ArgumentCaptor.forClass(String.class);
      verify(spyInternalApiClient)
          .cloneGeneration(stringCapture.capture(), argumentCapture.capture());
      final var request = argumentCapture.getValue();

      assertThat(request.operateVersion()).isEqualTo(OPERATE_IMAGE);
      assertThat(request.zeebeVersion()).isNull();
    }

    @Test
    public void shouldThrowExceptionIfGenerationWasNotCreated() {
      // given
      final var brokenStubInternalApiClient = new StubExternalConsoleAPIClient(true);
      final var spyStubInternalApiClient = spy(brokenStubInternalApiClient);

      final var sutLocal = new CreateGenerationInCamundaCloudHandler(spyStubInternalApiClient);

      // when + then
      assertThatThrownBy(() -> sutLocal.handle(jobClientStub, activatedJobStub))
          .isExactlyInstanceOf(RuntimeException.class)
          .hasMessageContaining("Creation of generation unsuccessful");
    }

    @Test
    void shouldSetGenerationNameAndUuidUponCompletion() throws Exception {
      // given
      final var initialGenerations =
          new ArrayList<>(stubExternalConsoleAPIClient.listGenerationInfos());

      // when
      sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub);

      // then
      assertThat(activatedJobStub).completed();

      final var createdGenerations =
          new ArrayList<>(stubExternalConsoleAPIClient.listGenerationInfos());
      createdGenerations.removeAll(initialGenerations);

      assertThat(createdGenerations).hasSize(1);

      final var createdGeneration = createdGenerations.get(0);

      assertThat(activatedJobStub)
          .completed()
          .extractingOutput()
          .containsOnly(
              entry("generation", createdGeneration.name()),
              entry("generationUUID", createdGeneration.uuid()));
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
