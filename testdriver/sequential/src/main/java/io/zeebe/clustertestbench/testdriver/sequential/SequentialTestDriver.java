package io.zeebe.clustertestbench.testdriver.sequential;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.CamundaCloudAuthenticationDetails;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.TestReport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequentialTestDriver {

  private static final Logger LOGGER = LoggerFactory.getLogger(SequentialTestDriver.class);

  private static final DateTimeFormatter INSTANT_FORMATTER =
      DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
          .withLocale(Locale.US)
          .withZone(ZoneId.systemDefault());

  private static final String KEY_ITERATION = "iteration";
  private static final String KEY_PROCESS_INSTANCE = "processInstanceKey";
  private static final String KEY_START_TIME = "startTime";

  private static final String JOB_TYPE = "test-job";
  private static final String PROCESS_ID = "sequential-test-process";

  private final ZeebeClient client;
  private final SequentialTestParameters testParameters;

  public SequentialTestDriver(
      final CamundaCloudAuthenticationDetails authenticationDetails,
      final SequentialTestParameters testParameters) {
    LOGGER.info("Creating Sequential Test Driver");
    final OAuthCredentialsProvider cred =
        requireNonNull(authenticationDetails).buildCredentialsProvider();

    client =
        ZeebeClient.newClientBuilder()
            .gatewayAddress(authenticationDetails.contactPoint())
            .credentialsProvider(cred)
            .build();

    this.testParameters = requireNonNull(testParameters);

    createAndDeploySequentialProcess();
  }

  private void createAndDeploySequentialProcess() {
    AbstractFlowNodeBuilder<?, ?> builder = Bpmn.createExecutableProcess(PROCESS_ID).startEvent();
    for (int i = 0; i < testParameters.getSteps(); i++) {
      builder =
          builder
              .serviceTask(String.format("step-%d", i))
              .zeebeJobType(JOB_TYPE)
              .zeebeJobRetries("0");
    }
    final var process = builder.endEvent().done();

    LOGGER.info("Deploying test process:" + PROCESS_ID);
    client.newDeployCommand().addProcessModel(process, PROCESS_ID + ".bpmn").send().join();
  }

  public TestReport runTest() {
    LOGGER.info("Starting Sequential Test ");

    try (final TestReportImpl testReport = new TestReportImpl(buildTestReportMetaData());
        final TestTimingContext overallTimingContext =
            new TestTimingContext(
                testParameters.getMaxTimeForCompleteTest(),
                "Test exceeded maximum time of " + testParameters.getMaxTimeForCompleteTest(),
                testReport::addFailure)) {
      final Duration timeForIteration = testParameters.getMaxTimeForIteration();

      final JobWorker workerRegistration =
          client
              .newWorker()
              .jobType(JOB_TYPE)
              .handler(new MoveAlongJobHandler())
              .timeout(Duration.ofSeconds(10))
              .open();

      for (int i = 0; i < testParameters.getIterations(); i++) {
        try (final TestTimingContext iterationTimingContxt =
            new TestTimingContext(
                timeForIteration,
                "Iteration " + i + " exceeded maximum time of " + timeForIteration,
                testReport::addFailure)) {

          final var variables = new HashMap<String, Object>();
          variables.put(
              KEY_START_TIME, convertMillisToString(iterationTimingContxt.getStartTime()));
          variables.put(KEY_ITERATION, i);

          final var result =
              client
                  .newCreateInstanceCommand()
                  .bpmnProcessId(PROCESS_ID)
                  .latestVersion()
                  .variables(variables)
                  .withResult()
                  .requestTimeout(timeForIteration.multipliedBy(2))
                  .send()
                  .join();

          iterationTimingContxt.putMetaData(KEY_PROCESS_INSTANCE, result.getProcessInstanceKey());

        } catch (final Exception e) {
          final var exceptionFilter =
              new ExceptionFilterBuilder() //
                  .ignoreRessourceExhaustedExceptions() //
                  // can occur because deployment needs to be distributed to other partitions
                  .ignoreProcessNotFoundExceptions(PROCESS_ID) //
                  .build();

          if (exceptionFilter.test(e)) {
            testReport.addFailure(
                "Exception in iteration "
                    + i
                    + ":"
                    + e.getMessage()
                    + " caused by "
                    + ofNullable(e.getCause())
                        .map(Throwable::getMessage)
                        .orElse("[cuase is empty]"));
          } else {
            // repeat iteration
            i--;
          }
        }
      }

      workerRegistration.close();

      return testReport;
    } finally {
      client.close();
    }
  }

  private static String convertMillisToString(final long millis) {
    final Instant instant = Instant.ofEpochMilli(millis);

    return INSTANT_FORMATTER.format(instant);
  }

  private Map<String, Object> buildTestReportMetaData() {
    return Map.of("testParams", testParameters);
  }

  private static class MoveAlongJobHandler implements JobHandler {
    @Override
    public void handle(final JobClient client, final ActivatedJob job) {
      LOGGER.info(job.toString());
      client.newCompleteCommand(job.getKey()).send().join();
    }
  }
}
