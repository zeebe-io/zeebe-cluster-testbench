package io.zeebe.clustertestbench.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.testdriver.api.CamundaCloudAuthenticationDetails;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.api.TestReport;
import io.zeebe.clustertestbench.testdriver.impl.CamundaCLoudAuthenticationDetailsImpl;
import io.zeebe.clustertestbench.testdriver.sequential.SequentialTestDriver;
import io.zeebe.clustertestbench.testdriver.sequential.SequentialTestParameters;
import io.zeebe.clustertestbench.util.LogDetails;

public class SequentialTestHandler implements JobHandler {

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    LogDetails.setMDCForJob(job);
    final Input input = job.getVariablesAsType(Input.class);

    final Thread testDiverThread =
        new Thread(
            () -> {
              final SequentialTestDriver sequentialTestDriver =
                  new SequentialTestDriver(
                      input.getAuthenticationDetails(), input.getTestParameters());

              final TestReport testReport = sequentialTestDriver.runTest();

              client.newCompleteCommand(job.getKey()).variables(new Output(testReport)).send();
            });

    testDiverThread.start();
  }

  private static final class Input {
    private CamundaCLoudAuthenticationDetailsImpl authenticationDetails;
    private SequentialTestParameters testParameters;

    @JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
    public CamundaCLoudAuthenticationDetailsImpl getAuthenticationDetails() {
      return authenticationDetails;
    }

    @JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
    public void setAuthenticationDetails(
        final CamundaCLoudAuthenticationDetailsImpl authenticationDetails) {
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
