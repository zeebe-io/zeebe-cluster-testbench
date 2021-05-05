package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.vavr.control.Either;
import io.zeebe.clustertestbench.cloud.response.ChannelInfo;
import io.zeebe.clustertestbench.cloud.response.GenerationInfo;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient;
import io.zeebe.clustertestbench.internal.cloud.request.CreateGenerationRequest;
import io.zeebe.clustertestbench.internal.cloud.request.UpdateChannelRequest;
import io.zeebe.clustertestbench.util.StringLookup;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;

public class CreateGenerationInCamundaCloudHandler implements JobHandler {

  private final InternalCloudAPIClient internalApiClient;

  public CreateGenerationInCamundaCloudHandler(final InternalCloudAPIClient internalApiClient) {
    this.internalApiClient = internalApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final var input = job.getVariablesAsType(Input.class);

    final var zeebeImage = input.getZeebeImage();

    final var generation = createGenerationName();
    final var templateGeneration = lookupTemplate(input.getGenerationTemplate());
    final var channelInfo = lookupChannel(input.getChannel());

    final var generationUUID = createGeneration(generation, zeebeImage, templateGeneration);

    try {
      addGenerationToChannel(channelInfo, generationUUID);

      final var output = new Output(generation, generationUUID);

      client.newCompleteCommand(job.getKey()).variables(output).send().join();
    } catch (final Exception e) {
      internalApiClient.deleteGeneration(generationUUID);
      throw e;
    }
  }

  private String createGeneration(
      final String generation, final String zeebeImage, final GenerationInfo template) {
    final var versions = new HashMap<>(template.getVersions());

    versions.put("zeebe", zeebeImage);

    final var createGenerationRequest =
        new CreateGenerationRequest(generation, versions, Collections.emptyList());

    internalApiClient.createGeneration(createGenerationRequest);

    return findGenerationInfoByName(generation)
        .getOrElseThrow(
            msg -> new RuntimeException("Creation of generation unsuccessful: " + generation))
        .getUuid();
  }

  private void addGenerationToChannel(final ChannelInfo channelInfo, final String generationUUID) {
    final List<String> allowedGenerationIds =
        channelInfo.getAllowedGenerations().stream()
            .map(GenerationInfo::getUuid)
            .collect(Collectors.toList());
    allowedGenerationIds.add(generationUUID);

    final var updateChannelRequest =
        new UpdateChannelRequest(
            channelInfo.getName(),
            channelInfo.isDefault(),
            channelInfo.getDefaultGeneration().getUuid(),
            allowedGenerationIds);

    internalApiClient.updateChannel(channelInfo.getUuid(), updateChannelRequest);
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
        new StringLookup<GenerationInfo>(
            "generation", name, generationInfos, GenerationInfo::getName, true);

    return generationLookup.lookup();
  }

  private ChannelInfo lookupChannel(final String channel) throws Exception {
    final var channelInfos = internalApiClient.listChannelInfos();

    final var channelLookup =
        new StringLookup<ChannelInfo>("channel", channel, channelInfos, ChannelInfo::getName, true);

    return channelLookup.lookup().getOrElseThrow(msg -> new IllegalArgumentException(msg));
  }

  protected static final class Input {

    private String generationTemplate;
    private String zeebeImage;
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
