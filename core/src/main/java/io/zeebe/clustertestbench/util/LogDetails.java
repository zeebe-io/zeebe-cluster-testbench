package io.zeebe.clustertestbench.util;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import java.util.Map;
import org.slf4j.MDC;

public final class LogDetails {

  private LogDetails() {}

  public static void setMDCForJob(final ActivatedJob job) {
    MDC.put("jobType", job.getType());
    MDC.put("processInstanceKey", Long.toString(job.getProcessInstanceKey()));

    final var variables = job.getVariablesAsMap();
    putVariableInMDCIfExists(variables, "clusterId");
    putVariableInMDCIfExists(variables, "clusterName");
    putVariableInMDCIfExists(variables, "clusterPlan");
    putVariableInMDCIfExists(variables, "clusterPlanUUID");
    putVariableInMDCIfExists(variables, "channel");
    putVariableInMDCIfExists(variables, "channelUUID");
    putVariableInMDCIfExists(variables, "generation");
    putVariableInMDCIfExists(variables, "generationUUID");
    putVariableInMDCIfExists(variables, "region");
    putVariableInMDCIfExists(variables, "regionUUID");
    putVariableInMDCIfExists(variables, "zeebeImage");
  }

  private static void putVariableInMDCIfExists(
      final Map<String, Object> variables, final String name) {
    if (!variables.containsKey(name)) {
      return;
    }

    final var variable = variables.get(name);

    final String value;
    if (variable == null) {
      value = null;
    } else if (variable instanceof String variableString) {
      value = variableString;
    } else {
      value = String.valueOf(variable);
    }
    MDC.put(name, value);
  }
}
