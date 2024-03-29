package io.zeebe.clustertestbench.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.notification.NotificationService;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.TestReportDTO;
import org.apache.commons.lang3.StringUtils;

public class NotifyEngineersHandler implements JobHandler {

  private static final int TEST_FAILURE_SUMMARY_ITEMS = 10;

  private final NotificationService notificationService;

  public NotifyEngineersHandler(final NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);
    final var message = composeMessage(input);

    notificationService.sendNotification(message);
    client.newCompleteCommand(job.getKey()).send();
  }

  protected String composeMessage(final Input input) {
    final StringBuilder resultBuilder = new StringBuilder();

    // icon
    resultBuilder.append(":bpmn-error-throw-event:");

    resultBuilder.append("\n");

    // message
    final var errorMessage =
        String.format(
            "_%s_ on _%s_ failed for branch `%s`. %n Zeebe image: _%s_ %n Generation: _%s_ %n Cluster : _%s_ `%s`",
            input.getTestProcessId(),
            input.getClusterPlan(),
            input.getBranch(),
            input.getZeebeImage(),
            input.getGeneration(),
            input.getClusterName(),
            input.getClusterId());

    resultBuilder.append(errorMessage);

    resultBuilder.append("\n");

    // operate link
    resultBuilder
        .append("<") //
        .append(input.getOperateURL()) //
        .append("|") //
        .append("Operate") //
        .append(">");

    if (StringUtils.isNotEmpty(input.getBusinessKey())) {
      resultBuilder.append("\n");

      resultBuilder.append("Business Key:").append(input.getBusinessKey());
    }

    resultBuilder.append("\n");

    // number of test failures
    resultBuilder
        .append("There were ") //
        .append(input.getTestReport().failureCount()) //
        .append(" failures.");

    input.getTestReport().failureMessages().stream() //
        .limit(TEST_FAILURE_SUMMARY_ITEMS) //
        .forEachOrdered(msg -> resultBuilder.append("\n").append(msg));

    if (input.getTestReport().failureCount() > TEST_FAILURE_SUMMARY_ITEMS) {
      resultBuilder.append("\n...\n");
    }

    return resultBuilder.toString();
  }

  static final class Input {
    private String generation;
    private String clusterPlan;
    private String clusterId;
    private String clusterName;
    private String operateURL;
    private String testProcessId;
    private String businessKey;

    private TestReportDTO testReport;
    private String zeebeImage;
    private String branch;

    public String getGeneration() {
      return generation;
    }

    public void setGeneration(final String generation) {
      this.generation = generation;
    }

    public String getClusterPlan() {
      return clusterPlan;
    }

    public void setClusterPlan(final String clusterPlan) {
      this.clusterPlan = clusterPlan;
    }

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

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
    public TestReportDTO getTestReport() {
      return testReport;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
    public void setTestReport(final TestReportDTO testReport) {
      this.testReport = testReport;
    }

    public String getOperateURL() {
      return operateURL;
    }

    public void setOperateURL(final String operateURL) {
      this.operateURL = operateURL;
    }

    public String getTestProcessId() {
      return testProcessId;
    }

    public void setTestProcessId(final String testProcessId) {
      this.testProcessId = testProcessId;
    }

    public String getBusinessKey() {
      return businessKey;
    }

    public void setBusinessKey(final String businessKey) {
      this.businessKey = businessKey;
    }

    public String getZeebeImage() {
      return zeebeImage;
    }

    public void setZeebeImage(final String zeebeImage) {
      this.zeebeImage = zeebeImage;
    }

    public String getBranch() {
      return branch;
    }

    public void setBranch(final String branch) {
      this.branch = branch;
    }
  }
}
