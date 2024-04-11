package io.zeebe.clustertestbench;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import io.camunda.zeebe.process.test.filters.RecordStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@ZeebeProcessTest
public class QaGithubTriggerTest {

  public static final List<String> JOB_TYPES =
      List.of(
          "create-generation-in-camunda-cloud-job",
          "trigger-message-start-event-job",
          "map-names-to-uuids-job",
          "destroy-zeebe-cluster-in-camunda-cloud-job",
          "io.camunda:slack:1");
  private static final JobHandler AUTO_COMPLETE_HANDLER = (c, j) -> c.newCompleteCommand(j).send();

  private static final JobHandler TEST_COMPLETE_HANDLER =
      (c, j) -> {
        final var authenticationDetails = j.getVariable("authenticationDetails");

        if (authenticationDetails == null) {
          c.newFailCommand(j).retries(0).errorMessage("Authentication Details missing");
          return;
        }

        c.newCompleteCommand(j)
            .variables("{\"testReport\": { \"testResult\": \"PASSED\" } }")
            .send();
      };

  private RecordStream streamSource;
  private ZeebeClient client;

  private Map<String, JobWorker> jobWorkers;

  private static BpmnModelInstance noopModelWithId(final String processId) {
    return Bpmn.createExecutableProcess(processId).startEvent().endEvent().done();
  }

  private static BpmnModelInstance oneTaskModelWithIdAndJobType(
      final String processId, final String jobType) {
    return Bpmn.createExecutableProcess(processId)
        .startEvent()
        .serviceTask(jobType, serviceTaskBuilder -> serviceTaskBuilder.zeebeJobType(jobType))
        .endEvent()
        .done();
  }

  @BeforeEach
  public void setUp() {
    client
        .newDeployResourceCommand()
        .addResourceFromClasspath("processes/qa-github-trigger.bpmn")
        .addProcessModel(
            oneTaskModelWithIdAndJobType("sequential-test", "run-sequential-test-job"),
            "sequential-test.bpmn")
        .addProcessModel(oneTaskModelWithIdAndJobType("chaosToolkit", "chaos"), "chaosToolkit.bpmn")
        .addProcessModel(
            oneTaskModelWithIdAndJobType(
                "prepare-zeebe-cluster-in-camunda-cloud", "cluster-creation"),
            "prepare-zeebe-cluster-in-camunda-cloud.bpmn")
        .send()
        .join();

    jobWorkers = new HashMap<>();
    for (final var jobType : JOB_TYPES) {
      final JobWorker worker =
          client.newWorker().jobType(jobType).handler(AUTO_COMPLETE_HANDLER).open();
      jobWorkers.put(jobType, worker);
    }

    jobWorkers.put(
        "chaos", client.newWorker().jobType("chaos").handler(TEST_COMPLETE_HANDLER).open());
    jobWorkers.put(
        "run-sequential-test-job",
        client
            .newWorker()
            .jobType("run-sequential-test-job")
            .handler(TEST_COMPLETE_HANDLER)
            .open());

    jobWorkers.put(
        "cluster-creation",
        client
            .newWorker()
            .jobType("cluster-creation")
            .handler(
                (c, j) -> {
                  c.newCompleteCommand(j).variables(Map.of("authenticationDetails", "{}")).send();
                })
            .open());

    jobWorkers.put(
        "aggregate-test-results-job",
        client
            .newWorker()
            .jobType("aggregate-test-results-job")
            .handler(
                (c, j) -> {
                  final var sequentialTestResult = j.getVariable("sequentialTestResult");

                  if (sequentialTestResult == null) {
                    c.newFailCommand(j).retries(0).errorMessage("Missing sequentialTestResult");
                    return;
                  }

                  final var chaosExperimentResult = j.getVariable("chaosExperimentResult");
                  if (chaosExperimentResult == null) {
                    c.newFailCommand(j).retries(0).errorMessage("Missing chaosExperimentResult");
                    return;
                  }

                  c.newCompleteCommand(j)
                      .variables("{\"testReport\": { \"testResult\": \"PASSED\" } }")
                      .send();
                })
            .open());

    BpmnAssert.initRecordStream(streamSource);
  }

  @AfterEach
  public void tearDown() {
    jobWorkers.forEach((t, w) -> w.close());
  }

  @Test
  public void shouldRunQaGithubTriggerToTheEnd() {
    // given

    // when
    final var instanceEvent =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId("qa-github-trigger")
            .latestVersion()
            .variables(
                Map.of(
                    "zeebeImage",
                    "image",
                    "generationTemplate",
                    "Zeebe SNAPSHOT",
                    "channel",
                    "Internal Dev",
                    "branch",
                    "main",
                    "build",
                    "link",
                    "businessKey",
                    "123",
                    "processId",
                    "qa-protocol"))
            .send()
            .join();

    // then
    Awaitility.await("process should be completed")
        .untilAsserted(
            () -> {
              BpmnAssert.initRecordStream(streamSource);
              final ProcessInstanceAssert assertions = BpmnAssert.assertThat(instanceEvent);
              assertions.hasNoIncidents().isCompleted();
            });
  }
}
