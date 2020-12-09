package io.zeebe.clustertestbench.handler;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import org.apache.commons.lang3.StringUtils;

/** Checks whether a certain generation is used by any cluster. */
public class CheckGenerationUsageHandler implements JobHandler {

  private final CloudAPIClient cloudApiClient;

  public CheckGenerationUsageHandler(final CloudAPIClient cloudApiClient) {
    this.cloudApiClient = cloudApiClient;
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    final var input = job.getVariablesAsType(Input.class);

    final var generationUUID = input.getGenerationUUID();

    if (StringUtils.isEmpty(generationUUID)) {
      throw new IllegalArgumentException("Field 'generationUUID' must not be null o rempty");
    }

    final var clusterInfos = cloudApiClient.listClusterInfos();

    final var notInUse =
        clusterInfos.stream()
            .filter(clusterInfo -> generationUUID.equals(clusterInfo.getGeneration().getUuid()))
            .findAny()
            .isEmpty();
    client.newCompleteCommand(job.getKey()).variables(new Output(notInUse)).send().join();
  }

  static class Input {
    private String generationUUID;

    String getGenerationUUID() {
      return generationUUID;
    }

    void setGenerationUUID(String generationUUID) {
      this.generationUUID = generationUUID;
    }
  }

  static class Output {
    private final boolean generationNotInUse;

    public Output(boolean generationNotInUse) {
      this.generationNotInUse = generationNotInUse;
    }

    public boolean isGenerationNotInUse() {
      return generationNotInUse;
    }
  }
}
