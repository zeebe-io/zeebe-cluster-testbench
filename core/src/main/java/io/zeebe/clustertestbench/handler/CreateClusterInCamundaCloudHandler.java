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

  private static final Logger logger =
      LoggerFactory.getLogger(CreateClusterInCamundaCloudHandler.class);

  private static final RandomNameGenerator NAME_GENRATOR = new RandomNameGenerator();

  private final CloudAPIClient cloudApiClient;

  public CreateClusterInCamundaCloudHandler(final CloudAPIClient cloudApiClient) {
    this.cloudApiClient = cloudApiClient;
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    String name = NAME_GENRATOR.next();

    logger.info("Creating cluster" + name);
    CreateClusterResponse createClusterRepoonse =
        cloudApiClient.createCluster(
            new CreateClusterRequest(
                name,
                input.getClusterPlanUUID(),
                input.getChannelUUID(),
                input.getGenerationUUID(),
                input.getRegionUUID()));

    String clusterId = createClusterRepoonse.getClusterId();

    try {
      CreateZeebeClientResponse createZeebeClientResponse =
          cloudApiClient.createZeebeClient(
              clusterId, new CreateZeebeClientRequest(name + "_client"));

      ZeebeClientConnectiontInfo connectionInfo =
          cloudApiClient.getZeebeClientInfo(clusterId, createZeebeClientResponse.getClientId());

      client
          .newCompleteCommand(job.getKey())
          .variables(new Output(connectionInfo, name, clusterId))
          .send();
    } catch (Exception e) {
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

    public void setGenerationUUID(String generationUUID) {
      this.generationUUID = generationUUID;
    }

    public String getRegionUUID() {
      return regionUUID;
    }

    public void setRegionUUID(String regionUUID) {
      this.regionUUID = regionUUID;
    }

    public String getClusterPlanUUID() {
      return clusterPlanUUID;
    }

    public void setClusterPlanUUID(String clusterPlanUUID) {
      this.clusterPlanUUID = clusterPlanUUID;
    }

    public String getChannelUUID() {
      return channelUUID;
    }

    public void setChannelUUID(String channelUUID) {
      this.channelUUID = channelUUID;
    }
  }

  private static final class Output {

    private CamundaCLoudAuthenticationDetailsImpl authenticationDetails;
    private String clusterId;
    private String clusterName;

    public Output(ZeebeClientConnectiontInfo connectionInfo, String clusterName, String clusterId) {
      this.authenticationDetails =
          new CamundaCLoudAuthenticationDetailsImpl(
              connectionInfo.getZeebeAuthorizationServerUrl(),
              connectionInfo.getZeebeAudience(),
              connectionInfo.getZeebeAddress(),
              connectionInfo.getZeebeClientId(),
              connectionInfo.getZeebeClientSecret());
      this.clusterName = clusterName;
      this.clusterId = clusterId;
    }

    @JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
    public CamundaCLoudAuthenticationDetailsImpl getAuthenticationDetails() {
      return authenticationDetails;
    }

    @JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
    public void setAuthenticationDetails(
        CamundaCLoudAuthenticationDetailsImpl authenticationDetails) {
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
