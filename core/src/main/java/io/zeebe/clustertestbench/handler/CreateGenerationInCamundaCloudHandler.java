package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.vavr.control.Either;
import io.zeebe.clustertestbench.internal.cloud.ExternalConsoleAPIClient;
import io.zeebe.clustertestbench.internal.cloud.ExternalConsoleAPIClient.CloneGenerationRequest;
import io.zeebe.clustertestbench.internal.cloud.ExternalConsoleAPIClient.GenerationInfo;
import io.zeebe.clustertestbench.util.StringLookup;
import org.apache.commons.lang3.RandomStringUtils;

public class CreateGenerationInCamundaCloudHandler implements JobHandler {
  private final ExternalConsoleAPIClient consoleApiClient;

  public CreateGenerationInCamundaCloudHandler(final ExternalConsoleAPIClient consoleApiClient) {
    this.consoleApiClient = consoleApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final var input = job.getVariablesAsType(Input.class);

    final var generationName = createGenerationName();
    final var templateGeneration = lookupTemplate(input.getGenerationTemplate());

    final var zeebeImage = input.getZeebeImage();
    final var operateImage = input.getOperateImage();

    final var newGenerationInfo =
        cloneGeneration(templateGeneration.uuid(), generationName, zeebeImage, operateImage);
    final var output = new Output(generationName, newGenerationInfo.uuid());

    client.newCompleteCommand(job.getKey()).variables(output).send().join();
  }

  private GenerationInfo cloneGeneration(
      final String uuid,
      final String generationName,
      final String zeebeImage,
      final String operateImage) {

    final var createGenerationRequest =
        new CloneGenerationRequest(generationName, zeebeImage, operateImage);

    return consoleApiClient.cloneGeneration(uuid, createGenerationRequest);
  }

  protected static String createGenerationName() {
    return "temp_" + RandomStringUtils.randomAlphanumeric(10);
  }

  private GenerationInfo lookupTemplate(final String generationTemplate) {
    final var eitherTemplateGenerationInfo = findGenerationInfoByName(generationTemplate);

    return eitherTemplateGenerationInfo.getOrElseThrow(msg -> new IllegalArgumentException(msg));
  }

  private Either<String, GenerationInfo> findGenerationInfoByName(final String name) {
    final var generationInfos = consoleApiClient.listGenerationInfos();

    final var generationLookup =
        new StringLookup<>("generation", name, generationInfos, GenerationInfo::name, true);

    return generationLookup.lookup();
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

    public void setOperateImage(final String operateImage) {
      this.operateImage = operateImage;
    }

    public String getOptimizeImage() {
      return optimizeImage;
    }

    public void setOptimizeImage(final String optimizeImage) {
      this.optimizeImage = optimizeImage;
    }

    public String getTasklistImage() {
      return tasklistImage;
    }

    public void setTasklistImage(final String tasklistImage) {
      this.tasklistImage = tasklistImage;
    }

    public String getElasticImage() {
      return elasticImage;
    }

    public void setElasticImage(final String elasticImage) {
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

  protected record Output(String generation, String generationUUID) {

    @Override
    public String toString() {
      return "Output [generation=" + generation + ", generationUUID=" + generationUUID + "]";
    }
  }
}
