package io.zeebe.clustertestbench.handler;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;

public class GatherInformationAboutClusterInCamundaCloudHandler implements JobHandler {

  private final CloudAPIClient cloudApiClient;

  public GatherInformationAboutClusterInCamundaCloudHandler(CloudAPIClient cloudApiClient) {
    this.cloudApiClient = cloudApiClient;
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    String operateURL =
        cloudApiClient.getClusterInfo(input.getClusterId()).getStatus().getOperateUrl();

    client.newCompleteCommand(job.getKey()).variables(new Output(operateURL)).send();
  }

  private static final class Input {
    private String clusterId;

    public String getClusterId() {
      return clusterId;
    }

    public void setClusterId(String clusterId) {
      this.clusterId = clusterId;
    }
  }

  private static final class Output {

    private final String operateURL;

    public Output(String operateURL) {
      this.operateURL = operateURL;
    }

    public String getOperateURL() {
      return operateURL;
    }
  }
}
