package io.zeebe.clustertestbench.handler;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.CamundaCloudAuthenticationDetails;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarmUpClusterHandler implements JobHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(WarmUpClusterHandler.class);

  private static final String PROCESS_RESOURCE = "warmup.bpmn";
  private static final String PROCESS_ID = "warmup";
  private static final String JOB_TYPE = "task-job";

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    final CamundaCloudAuthenticationDetails authenticationDetails =
        input.getAuthenticationDetails();
    final OAuthCredentialsProvider cred =
        requireNonNull(authenticationDetails).buildCredentialsProvider();

    try (final ZeebeClient zeebeClient =
        ZeebeClient.newClientBuilder()
            .gatewayAddress(authenticationDetails.contactPoint())
            .credentialsProvider(cred)
            .build()) {

      LOGGER.info("Deploying test process:" + PROCESS_ID);
      zeebeClient.newDeployCommand().addResourceFromClasspath(PROCESS_RESOURCE).send().join();
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
              .bpmnProcessId(PROCESS_ID)
              .latestVersion()
              .withResult()
              .requestTimeout(Duration.ofSeconds(15))
              .send()
              .join();
        } catch (final Exception e) {
          // repeat iteration
          i--;
        }
      }

      client.newCompleteCommand(job.getKey()).send().join();
    }
  }

  private static class MoveAlongJobHandler implements JobHandler {
    @Override
    public void handle(final JobClient client, final ActivatedJob job) {
      LOGGER.info(job.toString());
      client.newCompleteCommand(job.getKey()).send().join();
    }
  }

  private static final class Input {
    private CamundaCloudAuthenticationDetails authenticationDetails;

    @JsonProperty(TestDriver.VARIABLE_KEY_AUTHENTICATION_DETAILS)
    public CamundaCloudAuthenticationDetails getAuthenticationDetails() {
      return authenticationDetails;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_AUTHENTICATION_DETAILS)
    public void setAuthenticationDetails(
        final CamundaCloudAuthenticationDetails authenticationDetails) {
      this.authenticationDetails = authenticationDetails;
    }
  }
}
