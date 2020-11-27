package io.zeebe.clustertestbench.handler;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;

public class DeleteClusterInCamundaCloudHandler implements JobHandler {

  private final CloudAPIClient cloudApiClient;

  public DeleteClusterInCamundaCloudHandler(final CloudAPIClient cloudApiClient) {
    this.cloudApiClient = cloudApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    cloudApiClient.deleteCluster(input.getClusterId());

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
