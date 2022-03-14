package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient;

public final class DeleteGenerationInCamundaCloudHandler implements JobHandler {

  private final InternalCloudAPIClient internalApiClient;

  public DeleteGenerationInCamundaCloudHandler(final InternalCloudAPIClient internalApiClient) {
    this.internalApiClient = internalApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    internalApiClient.deleteGeneration(input.getGenerationUUID());

    client.newCompleteCommand(job.getKey()).send().join();
  }

  protected static final class Input {
    private String generationUUID;

    public String getGenerationUUID() {
      return generationUUID;
    }

    public void setGenerationUUID(final String generationUUID) {
      this.generationUUID = generationUUID;
    }
  }
}
