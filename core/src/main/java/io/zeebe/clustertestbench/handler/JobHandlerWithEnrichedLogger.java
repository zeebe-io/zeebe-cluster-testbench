package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Job handler decorator that enriches the logging framework with context information from the
 * activated job.
 */
public final class JobHandlerWithEnrichedLogger implements JobHandler {
  private static final Logger LOG = LoggerFactory.getLogger(JobHandlerWithEnrichedLogger.class);
  private final JobHandler delegate;

  public JobHandlerWithEnrichedLogger(final JobHandler delegate) {
    this.delegate = delegate;
  }

  @Override
  @SuppressWarnings("unused")
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    LOG.debug("Start handling of job {}", job.getKey());
    try (final var loggingEnricher = new LoggingEnricher(job)) {
      delegate.handle(client, job);
    } catch (final Exception ex) {
      LOG.error("Handling of job {} failed, with {} ", job.getKey(), ex.getMessage(), ex);
      throw ex;
    } finally {
      LOG.debug("Completed handling of job {}", job.getKey());
    }
  }

  /**
   * This logging enricher uses MDC to provide context information to the logging framework. It's an
   * autoclosable to make sure the MDC is cleaned-up again after usage.
   */
  public static final class LoggingEnricher implements AutoCloseable {

    private static final String JOB_TYPE = "jobType";
    private static final String JOB_PROCESS_INSTANCE_KEY = "processInstanceKey";
    private static final List<String> JOB_VARIABLES =
        List.of(
            "clusterId",
            "clusterName",
            "clusterPlan",
            "clusterPlanUUID",
            "channel",
            "channelUUID",
            "generation",
            "generationUUID",
            "region",
            "regionUUID",
            "zeebeImage");

    LoggingEnricher(final ActivatedJob job) {
      putJobValuesInMDC(job);
    }

    @Override
    public void close() {
      MDC.remove(JOB_TYPE);
      MDC.remove(JOB_PROCESS_INSTANCE_KEY);
      JOB_VARIABLES.forEach(MDC::remove);
    }

    private void putJobValuesInMDC(final ActivatedJob job) {
      MDC.put(JOB_TYPE, job.getType());
      MDC.put(JOB_PROCESS_INSTANCE_KEY, Long.toString(job.getProcessInstanceKey()));

      final var variables = job.getVariablesAsMap();
      JOB_VARIABLES.forEach(name -> putVariableInMDCIfExists(variables, name));
    }

    private void putVariableInMDCIfExists(final Map<String, Object> variables, final String name) {
      if (!variables.containsKey(name)) {
        return;
      }

      final var variable = variables.get(name);

      final String value;
      if (variable == null) {
        value = null;
      } else if (variable instanceof final String variableString) {
        value = variableString;
      } else {
        value = String.valueOf(variable);
      }
      MDC.put(name, value);
    }
  }
}
