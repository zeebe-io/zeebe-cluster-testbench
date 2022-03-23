package io.zeebe.clustertestbench.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.CamundaCloudAuthenticationDetails;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.TestReport;
import io.zeebe.clustertestbench.testdriver.sequential.SequentialTestDriver;
import io.zeebe.clustertestbench.testdriver.sequential.SequentialTestParameters;

public class SequentialTestHandler implements JobHandler {

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    final Thread testDiverThread =
        new Thread(
            () -> {
              try {
                final SequentialTestDriver sequentialTestDriver =
                    new SequentialTestDriver(
                        input.getAuthenticationDetails(), input.getTestParameters());

                final TestReport testReport = sequentialTestDriver.runTest();

                client.newCompleteCommand(job.getKey()).variables(new Output(testReport)).send();
              } catch (final Exception e) {
                client
                    .newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.toString());
              }
            });

    testDiverThread.start();
  }

  private static final class Input {
    private CamundaCloudAuthenticationDetails authenticationDetails;
    private SequentialTestParameters testParameters;

    @JsonProperty(TestDriver.VARIABLE_KEY_AUTHENTICATION_DETAILS)
    public CamundaCloudAuthenticationDetails getAuthenticationDetails() {
      return authenticationDetails;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_AUTHENTICATION_DETAILS)
    public void setAuthenticationDetails(
        final CamundaCloudAuthenticationDetails authenticationDetails) {
      this.authenticationDetails = authenticationDetails;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_PARAMETERS)
    public SequentialTestParameters getTestParameters() {
      return testParameters;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_PARAMETERS)
    public void setTestParameters(final SequentialTestParameters testParameters) {
      this.testParameters = testParameters;
    }
  }

  private static final class Output {

    private final TestReport testReport;

    public Output(final TestReport testReport) {
      super();
      this.testReport = testReport;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
    public TestReport getTestReport() {
      return testReport;
    }
  }
}
