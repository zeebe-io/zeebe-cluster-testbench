package io.zeebe.clustertestbench.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.request.CreateClusterRequest;
import io.zeebe.clustertestbench.cloud.request.CreateZeebeClientRequest;
import io.zeebe.clustertestbench.cloud.response.CreateClusterResponse;
import io.zeebe.clustertestbench.cloud.response.CreateZeebeClientResponse;
import io.zeebe.clustertestbench.cloud.response.ZeebeClientConnectiontInfo;
import io.zeebe.clustertestbench.testdriver.api.CamundaCloudAuthenticationDetails;
import io.zeebe.clustertestbench.testdriver.impl.CamundaCLoudAuthenticationDetailsImpl;
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
    final Input input = job.getVariablesAsType(Input.class);

    final String name = NAME_GENRATOR.next();

    LOGGER.info("Creating cluster {}", name);
    final CreateClusterResponse createClusterRepoonse =
        cloudApiClient.createCluster(
            new CreateClusterRequest(
                name,
                input.getClusterPlanUUID(),
                input.getChannelUUID(),
                input.getGenerationUUID(),
                input.getRegionUUID()));

    final String clusterId = createClusterRepoonse.getClusterId();

    try {
      final CreateZeebeClientResponse createZeebeClientResponse =
          cloudApiClient.createZeebeClient(
              clusterId, new CreateZeebeClientRequest(name + "_client"));

      final ZeebeClientConnectiontInfo connectionInfo =
          cloudApiClient.getZeebeClientInfo(clusterId, createZeebeClientResponse.getClientId());

      client
          .newCompleteCommand(job.getKey())
          .variables(new Output(createZeebeClientResponse, connectionInfo, name, clusterId))
          .send();
    } catch (final Exception e) {
      cloudApiClient.deleteCluster(clusterId);

      client
          .newFailCommand(job.getKey())
          .retries(job.getRetries() - 1)
          .errorMessage("Error while creating stack trace: " + e.getMessage());
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

    private CamundaCLoudAuthenticationDetailsImpl authenticationDetails;
    private final String clusterId;
    private final String clusterName;

    public Output(
        final CreateZeebeClientResponse createZeebeClientResponse,
        final ZeebeClientConnectiontInfo connectionInfo,
        final String clusterName,
        final String clusterId) {
      this.authenticationDetails =
          new CamundaCLoudAuthenticationDetailsImpl(
              connectionInfo.getZeebeAuthorizationServerUrl(),
              connectionInfo.getZeebeAudience(),
              connectionInfo.getZeebeAddress(),
              createZeebeClientResponse.getClientId(),
              createZeebeClientResponse.getClientSecret());
      this.clusterName = clusterName;
      this.clusterId = clusterId;
    }

    @JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
    public CamundaCLoudAuthenticationDetailsImpl getAuthenticationDetails() {
      return authenticationDetails;
    }

    @JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
    public void setAuthenticationDetails(
        final CamundaCLoudAuthenticationDetailsImpl authenticationDetails) {
      this.authenticationDetails = authenticationDetails;
    }

    public String getClusterId() {
      return clusterId;
    }

    public String getClusterName() {
      return clusterName;
    }
  }
}
