package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.notification.NotificationService;
import io.zeebe.clustertestbench.util.LogDetails;
import org.apache.commons.lang3.StringUtils;

public class NotifyEngineersPrepareFailedHandler implements JobHandler {

  private final NotificationService notificationService;

  public NotifyEngineersPrepareFailedHandler(final NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    LogDetails.setMDCForJob(job);
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

    resultBuilder
        .append("Failed to run test on " + input.getClusterPlan())
        .append(" : ")
        .append(
            "Timed out while waiting for the cluster to be healthy. Most probably the cluster is deleted.");
    resultBuilder.append("\n");

    // message
    resultBuilder
        .append("Cluster name : ")
        .append(input.getClusterName())
        .append(" ( " + input.getClusterId() + " )");
    resultBuilder.append("\n");

    resultBuilder.append("Generation : ").append(input.getGeneration());
    resultBuilder.append("\n");
    resultBuilder.append("Region : ").append(input.getRegion());
    resultBuilder.append("\n");
    if (StringUtils.isNotEmpty(input.getBusinessKey())) {
      resultBuilder.append("Business Key:").append(input.getBusinessKey());
      resultBuilder.append("\n");
    }
    return resultBuilder.toString();
  }

  static final class Input {
    private String clusterName;
    private String businessKey;
    private String generation;
    private String region;
    private String clusterId;
    private String clusterPlan;

    public String getClusterId() {
      return clusterId;
    }

    public void setClusterId(final String clusterId) {
      this.clusterId = clusterId;
    }

    public String getGeneration() {
      return generation;
    }

    public void setGeneration(final String generation) {
      this.generation = generation;
    }

    public String getRegion() {
      return region;
    }

    public void setRegion(final String region) {
      this.region = region;
    }

    public String getClusterName() {
      return clusterName;
    }

    public void setClusterName(final String clusterName) {
      this.clusterName = clusterName;
    }

    public String getBusinessKey() {
      return businessKey;
    }

    public void setBusinessKey(final String businessKey) {
      this.businessKey = businessKey;
    }

    public String getClusterPlan() {
      return clusterPlan;
    }

    public void setClusterPlan(final String clusterPlan) {
      this.clusterPlan = clusterPlan;
    }
  }
}
