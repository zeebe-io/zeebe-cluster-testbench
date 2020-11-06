package io.zeebe.clustertestbench.handler;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import io.zeebe.clustertestbench.testdriver.api.CamundaCloudAuthenticationDetails;
import io.zeebe.clustertestbench.testdriver.impl.CamundaCLoudAuthenticationDetailsImpl;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarmUpClusterHandler implements JobHandler {

  private static final Logger logger = LoggerFactory.getLogger(WarmUpClusterHandler.class);

  private static final String WORKFLOW_RESOURCE = "warmup.bpmn";
  private static final String WORKFLOW_ID = "warmup";
  private static final String JOB_TYPE = "task-job";

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    final CamundaCloudAuthenticationDetails authenticationDetails =
        input.getAuthenticationDetails();
    final OAuthCredentialsProvider cred =
        buildCredentialsProvider(requireNonNull(authenticationDetails));

    try (final ZeebeClient zeebeClient =
        ZeebeClient.newClientBuilder()
            .brokerContactPoint(authenticationDetails.getContactPoint())
            .credentialsProvider(cred)
            .build()) {

      logger.info("Deploying test workflow:" + WORKFLOW_ID);
      zeebeClient.newDeployCommand().addResourceFromClasspath(WORKFLOW_RESOURCE).send().join();
      zeebeClient
          .newWorker()
          .jobType(JOB_TYPE)
          .handler(new MoveAlongJobHandler())
          .timeout(Duration.ofMinutes(1))
          .open();

      for (int i = 0; i < 10; i++) {

        try {
          zeebeClient
              .newCreateInstanceCommand()
              .bpmnProcessId(WORKFLOW_ID)
              .latestVersion()
              .withResult()
              .requestTimeout(Duration.ofSeconds(15))
              .send()
              .join();
        } catch (Exception e) {
          // repeat iteration
          i--;
        }
      }

      client.newCompleteCommand(job.getKey()).send().join();
    }
  }

  private OAuthCredentialsProvider buildCredentialsProvider(
      CamundaCloudAuthenticationDetails authenticationDetails) {
    if (authenticationDetails.getAuthorizationURL() == null) {
      return new OAuthCredentialsProviderBuilder()
          .audience(authenticationDetails.getAudience())
          .clientId(authenticationDetails.getClientId())
          .clientSecret(authenticationDetails.getClientSecret())
          .build();
    } else {
      return new OAuthCredentialsProviderBuilder()
          .authorizationServerUrl(authenticationDetails.getAuthorizationURL())
          .audience(authenticationDetails.getAudience())
          .clientId(authenticationDetails.getClientId())
          .clientSecret(authenticationDetails.getClientSecret())
          .build();
    }
  }

  private static class MoveAlongJobHandler implements JobHandler {
    @Override
    public void handle(final JobClient client, final ActivatedJob job) {
      logger.info(job.toString());
      client.newCompleteCommand(job.getKey()).send().join();
    }
  }

  private static final class Input {
    private CamundaCLoudAuthenticationDetailsImpl authenticationDetails;

    @JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
    public CamundaCLoudAuthenticationDetailsImpl getAuthenticationDetails() {
      return authenticationDetails;
    }

    @JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
    public void setAuthenticationDetails(
        CamundaCLoudAuthenticationDetailsImpl authenticationDetails) {
      this.authenticationDetails = authenticationDetails;
    }
  }
}
