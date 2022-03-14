package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;

public class DeleteClusterInCamundaCloudHandler implements JobHandler {

  private final CloudAPIClient cloudApiClient;

  public DeleteClusterInCamundaCloudHandler(final CloudAPIClient cloudApiClient) {
    this.cloudApiClient = cloudApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    final var clusterId = input.getClusterId();
    cloudApiClient.listClusterInfos().stream()
        // check whether cluster still exists
        .filter(clusterInfo -> clusterId.equals(clusterInfo.uuid()))
        .findFirst()
        .ifPresent(
            (clusterInfo) -> {
              // if so, delete it
              cloudApiClient.deleteCluster(clusterId);
            });

    client.newCompleteCommand(job.getKey()).send();
  }

  private static final class Input {
    private String clusterId;

    public String getClusterId() {
      return clusterId;
    }

    public void setClusterId(final String clusterId) {
      this.clusterId = clusterId;
    }
  }
}
