package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.ClusterInfo;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.ClusterStatus;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryClusterStateInCamundaCloudHandler implements JobHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(QueryClusterStateInCamundaCloudHandler.class);

  private final CloudAPIClient cloudApiClient;

  public QueryClusterStateInCamundaCloudHandler(final CloudAPIClient cloudApiClient) {
    this.cloudApiClient = cloudApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    final String clusterStatus =
        Optional.ofNullable(cloudApiClient.getClusterInfo(input.getClusterId()))
            .map(ClusterInfo::status)
            .map(ClusterStatus::ready)
            .orElse("Unknown");

    LOGGER.info("Status of cluster " + input.getClusterName() + " " + clusterStatus);

    client.newCompleteCommand(job.getKey()).variables(new Output(clusterStatus)).send();
  }

  private static final class Input {
    private String clusterId;
    private String clusterName;

    public String getClusterId() {
      return clusterId;
    }

    public void setClusterId(final String clusterId) {
      this.clusterId = clusterId;
    }

    public String getClusterName() {
      return clusterName;
    }

    public void setClusterName(final String clusterName) {
      this.clusterName = clusterName;
    }
  }

  private static final class Output {

    private final String clusterStatus;

    public Output(final String clusterStatus) {
      this.clusterStatus = clusterStatus;
    }

    public String getClusterStatus() {
      return clusterStatus;
    }
  }
}
