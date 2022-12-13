package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.vavr.control.Either;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient.ChannelInfo;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient.CreateGenerationRequest;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient.GenerationInfo;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient.UpdateChannelRequest;
import io.zeebe.clustertestbench.util.StringLookup;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;

public class CreateGenerationInCamundaCloudHandler implements JobHandler {

  private static final String KEY_ZEEBE_IMAGE = "zeebe";
  private static final String KEY_OPERATE_IMAGE = "operate";
  private static final String KEY_OPTIMIZE_IMAGE = "optimize";
  private static final String KEY_TASKLIST_IMAGE = "tasklist";
  private static final String KEY_ELASTIC_IMAGE = "elasticSearchOss";

  private final InternalCloudAPIClient internalApiClient;

  public CreateGenerationInCamundaCloudHandler(final InternalCloudAPIClient internalApiClient) {
    this.internalApiClient = internalApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final var input = job.getVariablesAsType(Input.class);

    final var generation = createGenerationName();
    final var templateGeneration = lookupTemplate(input.getGenerationTemplate());
    final var channelInfo = lookupChannel(input.getChannel());

    final var versionsUnderTest = determineVersionsUnderTest(input);
    final var generationUUID = createGeneration(generation, templateGeneration, versionsUnderTest);

    try {
      addGenerationToChannel(channelInfo, generationUUID);

      final var output = new Output(generation, generationUUID);

      client.newCompleteCommand(job.getKey()).variables(output).send().join();
    } catch (final Exception e) {
      internalApiClient.deleteGeneration(generationUUID);
      throw e;
    }
  }

  private Map<String, String> determineVersionsUnderTest(final Input input) {
    final var versions = new HashMap<String, String>();

    final var zeebeImage = input.getZeebeImage();
    Optional.ofNullable(zeebeImage).ifPresent(v -> versions.put(KEY_ZEEBE_IMAGE, v));

    final var operateImage = input.getOperateImage();
    Optional.ofNullable(operateImage).ifPresent(v -> versions.put(KEY_OPERATE_IMAGE, v));

    final var optimizeImage = input.getOptimizeImage();
    Optional.ofNullable(optimizeImage).ifPresent(v -> versions.put(KEY_OPTIMIZE_IMAGE, v));

    final var tasklistImage = input.getTasklistImage();
    Optional.ofNullable(tasklistImage).ifPresent(v -> versions.put(KEY_TASKLIST_IMAGE, v));

    final var elasticImage = input.getElasticImage();
    Optional.ofNullable(elasticImage).ifPresent(v -> versions.put(KEY_ELASTIC_IMAGE, v));

    return versions;
  }

  private String createGeneration(
      final String generation,
      final GenerationInfo template,
      final Map<String, String> versionsUnderTest) {
    final var versions = new HashMap<>(template.versions());
    versions.putAll(versionsUnderTest);

    // old generations may contain a "worker" property
    // which is not accepted anymore with newer versions
    // of console
    versions.remove("worker");

    final var createGenerationRequest =
        new CreateGenerationRequest(generation, versions, Collections.emptyList());

    internalApiClient.createGeneration(createGenerationRequest);

    return findGenerationInfoByName(generation)
        .getOrElseThrow(
            msg -> new RuntimeException("Creation of generation unsuccessful: " + generation))
        .uuid();
  }

  private void addGenerationToChannel(final ChannelInfo channelInfo, final String generationUUID) {
    final List<String> allowedGenerationIds =
        channelInfo.allowedGenerations().stream()
            .map(GenerationInfo::uuid)
            .collect(Collectors.toList());
    allowedGenerationIds.add(generationUUID);

    final var updateChannelRequest =
        new UpdateChannelRequest(
            channelInfo.name(),
            channelInfo.isDefault(),
            channelInfo.defaultGeneration().uuid(),
            allowedGenerationIds);

    internalApiClient.updateChannel(channelInfo.uuid(), updateChannelRequest);
  }

  protected static String createGenerationName() {
    return "temp_" + RandomStringUtils.randomAlphanumeric(10);
  }

  private GenerationInfo lookupTemplate(final String generationTemplate) {
    final var eitherTemplateGenerationInfo = findGenerationInfoByName(generationTemplate);

    return eitherTemplateGenerationInfo.getOrElseThrow(msg -> new IllegalArgumentException(msg));
  }

  private Either<String, GenerationInfo> findGenerationInfoByName(final String name) {
    final var generationInfos = internalApiClient.listGenerationInfos();

    final var generationLookup =
        new StringLookup<>("generation", name, generationInfos, GenerationInfo::name, true);

    return generationLookup.lookup();
  }

  private ChannelInfo lookupChannel(final String channel) {
    final var channelInfos = internalApiClient.listChannelInfos();

    final var channelLookup =
        new StringLookup<>("channel", channel, channelInfos, ChannelInfo::name, true);

    return channelLookup.lookup().getOrElseThrow(msg -> new IllegalArgumentException(msg));
  }

  protected static final class Input {

    private String generationTemplate;
    private String zeebeImage;
    private String operateImage;
    private String optimizeImage;
    private String tasklistImage;
    private String elasticImage;
    private String channel;

    public String getGenerationTemplate() {
      return generationTemplate;
    }

    public void setGenerationTemplate(final String generationTemplate) {
      this.generationTemplate = generationTemplate;
    }

    public String getZeebeImage() {
      return zeebeImage;
    }

    public void setZeebeImage(final String zeebeImage) {
      this.zeebeImage = zeebeImage;
    }

    public String getOperateImage() {
      return operateImage;
    }

    public void setOperateImage(String operateImage) {
      this.operateImage = operateImage;
    }

    public String getOptimizeImage() {
      return optimizeImage;
    }

    public void setOptimizeImage(String optimizeImage) {
      this.optimizeImage = optimizeImage;
    }

    public String getTasklistImage() {
      return tasklistImage;
    }

    public void setTasklistImage(String tasklistImage) {
      this.tasklistImage = tasklistImage;
    }

    public String getElasticImage() {
      return elasticImage;
    }

    public void setElasticImage(String elasticImage) {
      this.elasticImage = elasticImage;
    }

    public String getChannel() {
      return channel;
    }

    public void setChannel(final String channel) {
      this.channel = channel;
    }

    @Override
    public String toString() {
      return "Input [generationTemplate="
          + generationTemplate
          + ", zeebeImage="
          + zeebeImage
          + ", operateImage="
          + operateImage
          + ", optimizeImage="
          + optimizeImage
          + ", tasklistImage="
          + tasklistImage
          + ", elasticImage="
          + elasticImage
          + ", channel="
          + channel
          + "]";
    }
  }

  protected static final class Output {
    private final String generation;
    private final String generationUUID;

    public Output(final String generation, final String generationUUID) {
      this.generation = generation;
      this.generationUUID = generationUUID;
    }

    public String getGeneration() {
      return generation;
    }

    public String getGenerationUUID() {
      return generationUUID;
    }

    @Override
    public String toString() {
      return "Output [generation=" + generation + ", generationUUID=" + generationUUID + "]";
    }
  }
}
