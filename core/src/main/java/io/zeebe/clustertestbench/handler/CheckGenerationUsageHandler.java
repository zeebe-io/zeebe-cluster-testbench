package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.util.LogDetails;
import org.apache.commons.lang3.StringUtils;

/** Checks whether a certain generation is used by any cluster. */
public class CheckGenerationUsageHandler implements JobHandler {

  private final CloudAPIClient cloudApiClient;

  public CheckGenerationUsageHandler(final CloudAPIClient cloudApiClient) {
    this.cloudApiClient = cloudApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    LogDetails.setMDCForJob(job);
    final var input = job.getVariablesAsType(Input.class);

    final var generationUUID = input.getGenerationUUID();

    if (StringUtils.isEmpty(generationUUID)) {
      throw new IllegalArgumentException("Field 'generationUUID' must not be null o rempty");
    }

    final var clusterInfos = cloudApiClient.listClusterInfos();

    final var notInUse =
        clusterInfos.stream()
            .filter(clusterInfo -> generationUUID.equals(clusterInfo.generation().uuid()))
            .findAny()
            .isEmpty();
    client.newCompleteCommand(job.getKey()).variables(new Output(notInUse)).send().join();
  }

  static class Input {
    private String generationUUID;

    public String getGenerationUUID() {
      return generationUUID;
    }

    public void setGenerationUUID(final String generationUUID) {
      this.generationUUID = generationUUID;
    }
  }

  static class Output {
    private final boolean generationNotInUse;

    public Output(final boolean generationNotInUse) {
      this.generationNotInUse = generationNotInUse;
    }

    public boolean isGenerationNotInUse() {
      return generationNotInUse;
    }
  }
}
