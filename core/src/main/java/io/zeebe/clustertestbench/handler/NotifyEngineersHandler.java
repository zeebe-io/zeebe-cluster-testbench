package io.zeebe.clustertestbench.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.notification.NotificationService;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.TestReportDTO;

public class NotifyEngineersHandler implements JobHandler {

  private static final int TEST_FAILURE_SUMMARY_ITEMS = 10;
  private static final String ZBCHAOS_LOG_URL =
      "https://console.cloud.google.com/logs/query;query=labels.clusterId%3D%22-CLUSTER_ID-%22;duration=PT6H?project=zeebe-io";

  private static final String FAILURE_MESSAGE_FORMAT =
      """
:bpmn-error-throw-event:
_%s_ on _%s_ failed for branch `%s`.
Check out here: %s

*Details:*

 * Zeebe image: _%s_
 * Generation: _%s_
 * Target cluster : _%s_ `%s`
 * Target cluster Operate: %s
 * Business key: %s
 * Zbchaos log: <%s|console log>

*Failures:*

Failure count: %d
""";

  private final NotificationService notificationService;
  private final String operateUrl;

  public NotifyEngineersHandler(
      final NotificationService notificationService, final String operateUrl) {
    this.notificationService = notificationService;
    this.operateUrl = operateUrl;
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

    final var errorMessage =
        String.format(
            FAILURE_MESSAGE_FORMAT,
            input.getTestProcessId(),
            input.getClusterPlan(),
            input.getBranch(),
            operateUrl,
            input.getZeebeImage(),
            input.getGeneration(),
            input.getClusterName(),
            input.getClusterId(),
            input.getOperateURL(),
            input.getBusinessKey(),
            ZBCHAOS_LOG_URL.replace("-CLUSTER_ID-", input.clusterId),
            input.getTestReport().failureCount());

    resultBuilder.append(errorMessage);

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
