package io.zeebe.clustertestbench.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.CreateZeebeClientRequest;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.CreateZeebeClientResponse;
import io.zeebe.clustertestbench.cloud.response.ZeebeClientConnectiontInfo;
import io.zeebe.clustertestbench.testdriver.api.CamundaCloudAuthenticationDetails;
import io.zeebe.clustertestbench.testdriver.impl.CamundaCLoudAuthenticationDetailsImpl;
import java.time.Duration;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateApiClientInCamundaCloudHandler implements JobHandler {

  // https://regex101.com/r/OVAMuf/1
  private static final String ZEEBE_ADDRESS_PATTERN = "([a-z0-9\\-]+\\.)([a-z0-9\\-]+)(\\.zeebe)";
  private static final Pattern PATTERN = Pattern.compile(ZEEBE_ADDRESS_PATTERN, Pattern.MULTILINE);
  private static final Logger LOGGER =
      LoggerFactory.getLogger(CreateApiClientInCamundaCloudHandler.class);

  private final CloudAPIClient cloudApiClient;

  public CreateApiClientInCamundaCloudHandler(final CloudAPIClient cloudApiClient) {
    this.cloudApiClient = cloudApiClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final var input = job.getVariablesAsType(Input.class);
    final String clusterId = input.getClusterId();
    final String clusterName = input.getClusterName();
    String clientId = null;

    try {
      LOGGER.info("Creating client for cluster {}", clusterId);
      final var createZeebeClientResponse =
          cloudApiClient.createZeebeClient(
              clusterId, new CreateZeebeClientRequest(clusterName + "_client"));
      clientId = createZeebeClientResponse.clientId();
      LOGGER.info("Created client {} for cluster {}", clientId, clusterId);

      ZeebeClientConnectiontInfo clientInfo;
      var i = 0;
      while ((clientInfo = getConnectionInfo(clusterId, clientId)) == null) {
        // todo: remove this workaround after https://github.com/camunda-cloud/console/issues/2225
        // After creating the cluster, the region information is not yet available in console.
        // It takes some time before the info is updated.
        Thread.sleep(Duration.ofSeconds(10).toMillis());
        if (i++ >= 3) {
          final var message =
              String.format(
                  "Expected to retrieve connection info for cluster %s and client %s, but fails even after retrying",
                  clusterId, clientId);
          LOGGER.error(message);
          failJob(client, job, clusterId, clientId, message);
          return;
        }
      }

      LOGGER.info("Connection info for client {} retrieved", clientId);
      client
          .newCompleteCommand(job.getKey())
          .variables(new Output(createZeebeClientResponse, clientInfo))
          .send();

    } catch (final Exception e) {
      final var message =
          String.format(
              "Expected to create a client and retrieve connection info for cluster %s, but failed because: %s",
              clusterId, e.getMessage());
      LOGGER.error(message, e);
      failJob(client, job, clusterId, clientId, message);
    }
  }

  @Nullable
  private ZeebeClientConnectiontInfo getConnectionInfo(
      final String clusterId, final String clientId) {
    LOGGER.info("Retrieving connection info for client {} in cluster {}", clientId, clusterId);
    final ZeebeClientConnectiontInfo clientInfo;
    try {
      clientInfo = cloudApiClient.getZeebeClientInfo(clusterId, clientId);
    } catch (final Exception e) {
      LOGGER.warn("Expected to retrieve connection info, but an exception occurred", e);
      return null;
    }

    if (!PATTERN.matcher(clientInfo.getZeebeAddress()).find()) {
      final var message =
          String.format(
              "Client %s info's zeebe address '%s' does not match '<uuid>.<region>.zeebe'",
              clientId, clientInfo.getZeebeAddress());
      LOGGER.warn(message);
      return null;
    }

    return clientInfo;
  }

  private void failJob(
      final JobClient client,
      final ActivatedJob job,
      final String clusterId,
      final String clientId,
      final String message) {
    try {
      if (clientId != null) {
        // delete client to keep worker idempotent
        LOGGER.info("Delete client {} for cluster {}", clientId, clusterId);
        cloudApiClient.deleteZeebeClient(clusterId, clientId);
      }
    } finally {
      client
          .newFailCommand(job.getKey())
          .retries(job.getRetries() - 1)
          .errorMessage(message)
          .send();
    }
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

    private CamundaCLoudAuthenticationDetailsImpl authenticationDetails;

    public Output(
        final CreateZeebeClientResponse createZeebeClientResponse,
        final ZeebeClientConnectiontInfo connectionInfo) {
      authenticationDetails =
          new CamundaCLoudAuthenticationDetailsImpl(
              connectionInfo.getZeebeAuthorizationServerUrl(),
              connectionInfo.getZeebeAudience(),
              connectionInfo.getZeebeAddress(),
              createZeebeClientResponse.clientId(),
              createZeebeClientResponse.clientSecret());
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
  }
}
