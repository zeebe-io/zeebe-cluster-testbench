package io.zeebe.clustertestbench.handler;

import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_CHANNEL_NAME;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_CHANNEL_UUID;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_ELASTIC_CURATOR_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_ELASTIC_OSS_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_GENERATION_NAME;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_OPERATE_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_OPTIMIZE_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_TASKLIST_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.DEFAULT_ZEEBE_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.KEY_ELASTIC_CURATOR_IMAGEE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.KEY_ELASTIC_OSS_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.KEY_OPERATE_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.KEY_OPTIMIZE_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.KEY_TASKLIST_IMAGE;
import static io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient.KEY_ZEEBE_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.camunda.community.zeebe.testutils.ZeebeWorkerAssertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient.CreateGenerationRequest;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient.GenerationInfo;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient.UpdateChannelRequest;
import io.zeebe.clustertestbench.internal.cloud.StubInternalCloudAPIClient;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateGenerationInCamundaCloudHandlerTest {
  private static final String ZEEBE_IMAGE = "zeebeTestImage";
  private static final String OPERATE_IMAGE = "operateTestImage";
  private static final String OPTIMIZE_IMAGE = "optimizeTestImage";
  private static final String TASKLIST_IMAGE = "tasklistTestImage";
  private static final String ELASTIC_IMAGE = "elasticTestImage";

  @Nested
  @DisplayName("Handle Job")
  class HandleJobTest {

    public static final String FIELD_ZEEBE_IMAGE = "zeebeImage";
    public static final String FIELD_OPERATE_IMAGE = "operateImage";
    public static final String FIELD_OPTIMIZE_IMAGE = "optimizeImage";
    public static final String FIELD_TASKLIST_IMAGE = "tasklistImage";
    public static final String FIELD_ELASTIC_IMAGE = "elasticImage";

    public static final String FIELD_GENERATION_TEMPLATE = "generationTemplate";
    public static final String FIELD_CHANNEL = "channel";
    final InternalCloudAPIClient stubInternalApiClient = new StubInternalCloudAPIClient(false);

    InternalCloudAPIClient spyInternalApiClient;

    CreateGenerationInCamundaCloudHandler sutCreateGenerationHandler;

    JobClientStub jobClientStub = new JobClientStub();

    ActivatedJobStub activatedJobStub;

    @BeforeEach
    public void setUp() {
      spyInternalApiClient = spy(stubInternalApiClient);
      sutCreateGenerationHandler = new CreateGenerationInCamundaCloudHandler(spyInternalApiClient);

      activatedJobStub = jobClientStub.createActivatedJob();

      activatedJobStub.setInputVariables(
          Map.of(
              FIELD_ZEEBE_IMAGE,
              ZEEBE_IMAGE,
              FIELD_GENERATION_TEMPLATE,
              DEFAULT_GENERATION_NAME,
              FIELD_CHANNEL,
              DEFAULT_CHANNEL_NAME));
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
    public void shouldThrowRuntimeExceptionIfChannelCannotBeFound() {
      // given
      activatedJobStub.setInputVariables(
          Map.of(
              FIELD_GENERATION_TEMPLATE,
              DEFAULT_GENERATION_NAME,
              FIELD_CHANNEL,
              "unknown-channel"));

      // when + then
      assertThatThrownBy(() -> sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub))
          .isExactlyInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unable to find channel")
          .hasMessageContaining("unknown-channel");
    }

    @Test
    public void shouldCreateGenerationWithDefaultVersions() throws Exception {
      // given
      activatedJobStub.setInputVariables(
          Map.of(
              FIELD_GENERATION_TEMPLATE,
              DEFAULT_GENERATION_NAME,
              FIELD_CHANNEL,
              DEFAULT_CHANNEL_NAME));

      // when
      sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(CreateGenerationRequest.class);
      verify(spyInternalApiClient).createGeneration(argumentCapture.capture());
      final var request = argumentCapture.getValue();

      assertThat(request.versions())
          .containsOnly(
              entry(KEY_ZEEBE_IMAGE, DEFAULT_ZEEBE_IMAGE),
              entry(KEY_OPERATE_IMAGE, DEFAULT_OPERATE_IMAGE),
              entry(KEY_OPTIMIZE_IMAGE, DEFAULT_OPTIMIZE_IMAGE),
              entry(KEY_TASKLIST_IMAGE, DEFAULT_TASKLIST_IMAGE),
              entry(KEY_ELASTIC_CURATOR_IMAGEE, DEFAULT_ELASTIC_CURATOR_IMAGE),
              entry(KEY_ELASTIC_OSS_IMAGE, DEFAULT_ELASTIC_OSS_IMAGE));
    }

    @Test
    public void shouldCreateGenerationWithSpecificZeebeVersion() throws Exception {
      // when
      sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(CreateGenerationRequest.class);
      verify(spyInternalApiClient).createGeneration(argumentCapture.capture());
      final var request = argumentCapture.getValue();

      assertThat(request.versions())
          .containsOnly(
              entry(KEY_ZEEBE_IMAGE, ZEEBE_IMAGE),
              entry(KEY_OPERATE_IMAGE, DEFAULT_OPERATE_IMAGE),
              entry(KEY_OPTIMIZE_IMAGE, DEFAULT_OPTIMIZE_IMAGE),
              entry(KEY_TASKLIST_IMAGE, DEFAULT_TASKLIST_IMAGE),
              entry(KEY_ELASTIC_CURATOR_IMAGEE, DEFAULT_ELASTIC_CURATOR_IMAGE),
              entry(KEY_ELASTIC_OSS_IMAGE, DEFAULT_ELASTIC_OSS_IMAGE));
    }

    @Test
    public void shouldCreateGenerationWithSpecificAppVersions() throws Exception {
      // given
      activatedJobStub.setInputVariables(
          Map.of(
              FIELD_OPERATE_IMAGE,
              OPERATE_IMAGE,
              FIELD_OPTIMIZE_IMAGE,
              OPTIMIZE_IMAGE,
              FIELD_TASKLIST_IMAGE,
              TASKLIST_IMAGE,
              FIELD_GENERATION_TEMPLATE,
              DEFAULT_GENERATION_NAME,
              FIELD_CHANNEL,
              DEFAULT_CHANNEL_NAME));

      // when
      sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(CreateGenerationRequest.class);
      verify(spyInternalApiClient).createGeneration(argumentCapture.capture());
      final var request = argumentCapture.getValue();

      assertThat(request.versions())
          .containsOnly(
              entry(KEY_ZEEBE_IMAGE, DEFAULT_ZEEBE_IMAGE),
              entry(KEY_OPERATE_IMAGE, OPERATE_IMAGE),
              entry(KEY_OPTIMIZE_IMAGE, OPTIMIZE_IMAGE),
              entry(KEY_TASKLIST_IMAGE, TASKLIST_IMAGE),
              entry(KEY_ELASTIC_CURATOR_IMAGEE, DEFAULT_ELASTIC_CURATOR_IMAGE),
              entry(KEY_ELASTIC_OSS_IMAGE, DEFAULT_ELASTIC_OSS_IMAGE));
    }

    @Test
    public void shouldCreateGenerationWithSpecificElasticVersion() throws Exception {
      // given
      activatedJobStub.setInputVariables(
          Map.of(
              FIELD_ELASTIC_IMAGE,
              ELASTIC_IMAGE,
              FIELD_GENERATION_TEMPLATE,
              DEFAULT_GENERATION_NAME,
              FIELD_CHANNEL,
              DEFAULT_CHANNEL_NAME));

      // when
      sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(CreateGenerationRequest.class);
      verify(spyInternalApiClient).createGeneration(argumentCapture.capture());
      final var request = argumentCapture.getValue();

      assertThat(request.versions())
          .containsOnly(
              entry(KEY_ZEEBE_IMAGE, DEFAULT_ZEEBE_IMAGE),
              entry(KEY_OPERATE_IMAGE, DEFAULT_OPERATE_IMAGE),
              entry(KEY_OPTIMIZE_IMAGE, DEFAULT_OPTIMIZE_IMAGE),
              entry(KEY_TASKLIST_IMAGE, DEFAULT_TASKLIST_IMAGE),
              entry(KEY_ELASTIC_CURATOR_IMAGEE, DEFAULT_ELASTIC_CURATOR_IMAGE),
              entry(KEY_ELASTIC_OSS_IMAGE, ELASTIC_IMAGE));
    }

    @Test
    public void shouldThrowExceptionIfGenerationWasNotCreated() {
      // given
      final var brokenStubInternalApiClient = new StubInternalCloudAPIClient(true);
      final var spyStubInternalApiClient = spy(brokenStubInternalApiClient);

      final var sutLocal = new CreateGenerationInCamundaCloudHandler(spyStubInternalApiClient);

      // when + then
      assertThatThrownBy(() -> sutLocal.handle(jobClientStub, activatedJobStub))
          .isExactlyInstanceOf(RuntimeException.class)
          .hasMessageContaining("Creation of generation unsuccessful");
    }

    @Test
    public void shouldAddGenerationToChannel() throws Exception {
      // when
      sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub);

      // then
      final var argumentCapture = ArgumentCaptor.forClass(UpdateChannelRequest.class);
      verify(spyInternalApiClient)
          .updateChannel(Mockito.eq(DEFAULT_CHANNEL_UUID), argumentCapture.capture());
      final var request = argumentCapture.getValue();

      final var generationUUIDs =
          stubInternalApiClient.listGenerationInfos().stream()
              .map(GenerationInfo::uuid)
              .collect(Collectors.toList());

      assertThat(request.name()).isEqualTo(StubInternalCloudAPIClient.DEFAULT_CHANNEL_NAME);
      assertThat(request.allowedGenerationIds()).containsExactlyElementsOf(generationUUIDs);
    }

    @Test
    void shouldSetGenerationNameAndUuidUponCompletion() throws Exception {
      // given
      final var initialGenerations = new ArrayList<>(stubInternalApiClient.listGenerationInfos());

      // when
      sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub);

      // then
      assertThat(activatedJobStub).completed();

      final var createdGenerations = new ArrayList<>(stubInternalApiClient.listGenerationInfos());
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

    @Test
    void shouldDeleteGenerationIfExceptionOccursAfterGenerationHasBennCreated() {
      // given
      final var message =
          "Deliberate runtime exception in shouldDeleteGenerationIfExceptionOccursAfterGenerationHasBennCreated";
      doThrow(new RuntimeException(message))
          .when(spyInternalApiClient)
          .updateChannel(Mockito.any(), Mockito.any());

      // when + then
      assertThatThrownBy(() -> sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub))
          .isExactlyInstanceOf(RuntimeException.class)
          .hasMessage(message);

      verify(spyInternalApiClient).deleteGeneration(Mockito.any());
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
