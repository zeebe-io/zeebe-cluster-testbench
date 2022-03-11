package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.util.LogDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatherInformationAboutClusterInCamundaCloudHandler implements JobHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(GatherInformationAboutClusterInCamundaCloudHandler.class);
  private final CloudAPIClient cloudApiClient;

  public GatherInformationAboutClusterInCamundaCloudHandler(final CloudAPIClient cloudApiClient) {
    this.cloudApiClient = cloudApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    LogDetails.setMDCForJob(job);
    final var input = job.getVariablesAsType(Input.class);
    final var clusterId = input.getClusterId();

    LOGGER.debug("Fetching cluster info for id {}", clusterId);
    final var clusterInfo = cloudApiClient.getClusterInfo(clusterId);

    final String operateURL = clusterInfo.links().operate();
    client.newCompleteCommand(job.getKey()).variables(new Output(operateURL)).send();
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

  private static final class Output {

    private final String operateURL;

    public Output(final String operateURL) {
      this.operateURL = operateURL;
    }

    public String getOperateURL() {
      return operateURL;
    }
  }
}
