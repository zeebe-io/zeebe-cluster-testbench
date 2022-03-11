package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.CreateClusterRequest;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.CreateClusterResponse;
import io.zeebe.clustertestbench.util.LogDetails;
import io.zeebe.clustertestbench.util.RandomNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateClusterInCamundaCloudHandler implements JobHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CreateClusterInCamundaCloudHandler.class);
  private static final RandomNameGenerator NAME_GENRATOR = new RandomNameGenerator();

  private final CloudAPIClient cloudApiClient;

  public CreateClusterInCamundaCloudHandler(final CloudAPIClient cloudApiClient) {
    this.cloudApiClient = cloudApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    LogDetails.setMDCForJob(job);
    final var input = job.getVariablesAsType(Input.class);

    final String name = NAME_GENRATOR.next();

    LOGGER.info("Creating cluster {}", name);
    CreateClusterResponse createClusterReponse = null;
    try {
      createClusterReponse =
          cloudApiClient.createCluster(
              new CreateClusterRequest(
                  name,
                  input.getClusterPlanUUID(),
                  input.getChannelUUID(),
                  input.getGenerationUUID(),
                  input.getRegionUUID()));

      final String clusterId = createClusterReponse.clusterId();
      LOGGER.info("Cluster {} ({}) created successfully", name, clusterId);

      client.newCompleteCommand(job.getKey()).variables(new Output(name, clusterId)).send();

    } catch (final Exception e) {
      final var message = String.format("Expected to create cluster %s, but failed", name);
      LOGGER.error(message, e);
      try {
        if (createClusterReponse != null) {
          // delete cluster to keep worker idempotent
          LOGGER.info("Delete cluster {}", createClusterReponse.clusterId());
          cloudApiClient.deleteCluster(createClusterReponse.clusterId());
        }
      } finally {
        client
            .newFailCommand(job.getKey())
            .retries(job.getRetries() - 1)
            .errorMessage(message)
            .send();
      }
    }
  }

  private static final class Input {
    private String generationUUID;
    private String regionUUID;
    private String clusterPlanUUID;
    private String channelUUID;

    public String getGenerationUUID() {
      return generationUUID;
    }

    public void setGenerationUUID(final String generationUUID) {
      this.generationUUID = generationUUID;
    }

    public String getRegionUUID() {
      return regionUUID;
    }

    public void setRegionUUID(final String regionUUID) {
      this.regionUUID = regionUUID;
    }

    public String getClusterPlanUUID() {
      return clusterPlanUUID;
    }

    public void setClusterPlanUUID(final String clusterPlanUUID) {
      this.clusterPlanUUID = clusterPlanUUID;
    }

    public String getChannelUUID() {
      return channelUUID;
    }

    public void setChannelUUID(final String channelUUID) {
      this.channelUUID = channelUUID;
    }
  }

  private static final class Output {

    private final String clusterId;
    private final String clusterName;

    public Output(final String clusterName, final String clusterId) {
      this.clusterName = clusterName;
      this.clusterId = clusterId;
    }

    public String getClusterId() {
      return clusterId;
    }

    public String getClusterName() {
      return clusterName;
    }
  }
}
