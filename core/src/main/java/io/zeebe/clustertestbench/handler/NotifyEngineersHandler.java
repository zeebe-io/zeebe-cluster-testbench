package io.zeebe.clustertestbench.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.notification.NotificationService;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.impl.TestReportDTO;

public class NotifyEngineersHandler implements JobHandler {

  private static final int TEST_FAILURE_SUMMARY_ITEMS = 10;

  private final NotificationService notificationService;

  public NotifyEngineersHandler(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);
    final var message = composeMessage(input);

    notificationService.sendNotification(message);
    client.newCompleteCommand(job.getKey()).send();
  }

  protected String composeMessage(Input input) {
    StringBuilder resultBuilder = new StringBuilder();

    // icon
    resultBuilder.append(":bpmn-error-throw-event:");

    resultBuilder.append("\n");

    // message
    resultBuilder
        .append("_")
        .append(input.getTestWorkflowId())
        .append("_") //
        .append(" on ")
        .append("_" + input.getClusterPlan() + "_") //
        .append(" failed for generation ")
        .append("`" + input.getGeneration() + "`") //
        .append(" on cluster ")
        .append("_" + input.getClusterName() + "_");

    resultBuilder.append("\n");

    // operate link
    resultBuilder
        .append("<") //
        .append(input.getOperateURL()) //
        .append("|") //
        .append("Operate") //
        .append(">");

    resultBuilder.append("\n");

    // number of test failures
    resultBuilder
        .append("There were ") //
        .append(input.getTestReport().getFailureCount()) //
        .append(" failures.");

    input.getTestReport().getFailureMessages().stream() //
        .limit(TEST_FAILURE_SUMMARY_ITEMS) //
        .forEachOrdered(msg -> resultBuilder.append(msg).append("\n"));

    if (input.getTestReport().getFailureCount() > TEST_FAILURE_SUMMARY_ITEMS) {
      resultBuilder.append("...\n");
    }

    return resultBuilder.toString();
  }

  static final class Input {
    private String generation;
    private String clusterPlan;
    private String clusterName;
    private String operateURL;
    private String testWorkflowId;

    private TestReportDTO testReport;

    public String getGeneration() {
      return generation;
    }

    public void setGeneration(String generation) {
      this.generation = generation;
    }

    public String getClusterPlan() {
      return clusterPlan;
    }

    public void setClusterPlan(String clusterPlan) {
      this.clusterPlan = clusterPlan;
    }

    public String getClusterName() {
      return clusterName;
    }

    public void setClusterName(String clusterName) {
      this.clusterName = clusterName;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
    public TestReportDTO getTestReport() {
      return testReport;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
    public void setTestReport(TestReportDTO testReport) {
      this.testReport = testReport;
    }

    public String getOperateURL() {
      return operateURL;
    }

    public void setOperateURL(String operateURL) {
      this.operateURL = operateURL;
    }

    public String getTestWorkflowId() {
      return testWorkflowId;
    }

    public void setTestWorkflowId(final String testWorkflowId) {
      this.testWorkflowId = testWorkflowId;
    }
  }
}
