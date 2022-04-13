package io.zeebe.clustertestbench.it.processes;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import io.camunda.zeebe.process.test.inspections.InspectionUtility;
import io.zeebe.clustertestbench.bootstrap.Launcher;
import io.zeebe.clustertestbench.handler.AggregateTestResultHandler;
import io.zeebe.clustertestbench.mockhandler.MockMapNamesToUUIDsHandler;
import io.zeebe.clustertestbench.mockhandler.NoOpHandler;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.TestReport;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.TestReportDTO;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.TestResult;
import io.zeebe.clustertestbench.testdriver.api.TestSuite.TestDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

@ZeebeProcessTest()
public class RunTestSuiteInCamundaCloudIT {

  public static final String RUN_TEST_SUITE_IN_CAMUNA_CLOUD = "run-test-suite-in-camuna-cloud";
  public static final String MOCK_TEST_RUNNER = "mock-test-runner";
  private ZeebeTestEngine engine;

  private ZeebeClient zeebeClient;

  @Test
  void shouldByDeployable() {
    // when + then
    assertThatNoException()
        .isThrownBy(
            () ->
                zeebeClient
                    .newDeployCommand()
                    .addResourceFile("../processes/run-test-suite-in-camunda-cloud.bpmn")
                    .send()
                    .join());
  }

  @Test
  void shouldCompleteOnHappyPath() throws InterruptedException, TimeoutException {
    // given
    final ArrayList<JobWorker> jobWorkers = registerWorkers();

    zeebeClient
        .newDeployCommand()
        .addResourceFile("../processes/run-test-suite-in-camunda-cloud.bpmn")
        .addResourceFromClasspath("it/runtestsuiteincamundacloud/mock-test-runner.bpmn")
        .addResourceFromClasspath(
            "it/runtestsuiteincamundacloud/mock-prepare-zeebe-cluster-in-camunda-cloud.bpmn")
        .send()
        .join();

    final var test =
        new TestDTO<Object>(
            MOCK_TEST_RUNNER,
            "generation-name",
            "channel-name",
            "region-name",
            Collections.singletonList("clusterplan-name"),
            null);

    final var testSuite = Collections.singletonList(test);

    // when
    final var result =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId(RUN_TEST_SUITE_IN_CAMUNA_CLOUD)
            .latestVersion()
            .variables(Map.of("testsuite", testSuite))
            .withResult()
            .send()
            .join();

    // then
    // --- test the final result of the process
    assertThat(result.getVariablesAsMap())
        .contains(Assertions.entry(AggregateTestResultHandler.KEY_AGGREGATED_RESULT, "PASSED"));

    // --- test the path that was taken
    assertThat(result)
        .hasPassedElementsInOrder(
            "map-names-to-uuids",
            "prepare-zeebe-cluster-in-camunda-cloud",
            "run-actual-test",
            "check_test_result",
            "destroy-zeebe-cluster-in-camunda-cloud",
            "aggregate-test-results-of-cluster-plans",
            "aggregate-test-results-of-tests");

    // --- test the input for "Run Test" call activity was passed in correctly
    final var calledTestProcessInstance =
        InspectionUtility.findProcessInstances()
            .withBpmnProcessId(MOCK_TEST_RUNNER)
            .findLastProcessInstance()
            .get();

    assertThat(calledTestProcessInstance)
        .hasVariableWithValue("channelUUID", "channel-name-uuid")
        .hasVariableWithValue("clusterPlanUUID", "clusterplan-name-uuid")
        .hasVariableWithValue("generationUUID", "generation-name-uuid")
        .hasVariableWithValue("regionUUID", "region-name-uuid");

    // cleanup
    jobWorkers.forEach(JobWorker::close);
  }

  @Test
  void shouldRunMultipleTests()
      throws InterruptedException, TimeoutException, JsonProcessingException {
    // given
    final ArrayList<JobWorker> jobWorkers = registerWorkers();

    final var deployment =
        zeebeClient
            .newDeployCommand()
            .addResourceFile("../processes/run-test-suite-in-camunda-cloud.bpmn")
            .addResourceFromClasspath("it/runtestsuiteincamundacloud/mock-test-runner.bpmn")
            .addResourceFromClasspath(
                "it/runtestsuiteincamundacloud/mock-prepare-zeebe-cluster-in-camunda-cloud.bpmn")
            .send()
            .join();

    final var test1 =
        new TestDTO<Object>(
            MOCK_TEST_RUNNER,
            "generation1-name",
            "channel1-name",
            "region1-name",
            Collections.singletonList("clusterplan1-name"),
            null);

    final var test2 =
        new TestDTO<Object>(
            MOCK_TEST_RUNNER,
            "generation2-name",
            "channel2-name",
            "region2-name",
            Collections.singletonList("clusterplan2-name"),
            null);

    final var testSuite = List.of(test1, test2);

    System.out.println(new ObjectMapper().writeValueAsString(testSuite));

    // when
    zeebeClient
        .newCreateInstanceCommand()
        .bpmnProcessId(RUN_TEST_SUITE_IN_CAMUNA_CLOUD)
        .latestVersion()
        .variables(Map.of("testsuite", testSuite))
        .withResult()
        .send()
        .join();

    // then
    BpmnAssert.assertThat(deployment)
        .extractingProcessByBpmnProcessId(MOCK_TEST_RUNNER)
        .hasInstances(2);

    // --- test the input for "Run Test" call activity was passed in correctly
    final var firstTestProcessInstance =
        InspectionUtility.findProcessInstances()
            .withBpmnProcessId(MOCK_TEST_RUNNER)
            .findFirstProcessInstance()
            .get();

    assertThat(firstTestProcessInstance)
        .hasVariableWithValue("channelUUID", "channel1-name-uuid")
        .hasVariableWithValue("clusterPlanUUID", "clusterplan1-name-uuid")
        .hasVariableWithValue("generationUUID", "generation1-name-uuid")
        .hasVariableWithValue("regionUUID", "region1-name-uuid");

    final var lastTestProcessInstance =
        InspectionUtility.findProcessInstances()
            .withBpmnProcessId(MOCK_TEST_RUNNER)
            .findLastProcessInstance()
            .get();

    assertThat(lastTestProcessInstance)
        .hasVariableWithValue("channelUUID", "channel2-name-uuid")
        .hasVariableWithValue("clusterPlanUUID", "clusterplan2-name-uuid")
        .hasVariableWithValue("generationUUID", "generation2-name-uuid")
        .hasVariableWithValue("regionUUID", "region2-name-uuid");

    // cleanup
    jobWorkers.forEach(JobWorker::close);
  }

  @Test
  void shouldRunSameTestInMultipleClusterPlans() throws InterruptedException, TimeoutException {
    // given
    final ArrayList<JobWorker> jobWorkers = registerWorkers();

    final var deployment =
        zeebeClient
            .newDeployCommand()
            .addResourceFile("../processes/run-test-suite-in-camunda-cloud.bpmn")
            .addResourceFromClasspath("it/runtestsuiteincamundacloud/mock-test-runner.bpmn")
            .addResourceFromClasspath(
                "it/runtestsuiteincamundacloud/mock-prepare-zeebe-cluster-in-camunda-cloud.bpmn")
            .send()
            .join();

    final var test =
        new TestDTO<Object>(
            MOCK_TEST_RUNNER,
            "generation-name",
            "channel-name",
            "region-name",
            List.of("clusterplan1-name", "clusterplan2-name"),
            null);

    final var testSuite = List.of(test);

    // when
    zeebeClient
        .newCreateInstanceCommand()
        .bpmnProcessId(RUN_TEST_SUITE_IN_CAMUNA_CLOUD)
        .latestVersion()
        .variables(Map.of("testsuite", testSuite))
        .withResult()
        .send()
        .join();

    // then
    BpmnAssert.assertThat(deployment)
        .extractingProcessByBpmnProcessId(MOCK_TEST_RUNNER)
        .hasInstances(2);

    // --- test the input for "Run Test" call activity was passed in correctly
    final var firstTestProcessInstance =
        InspectionUtility.findProcessInstances()
            .withBpmnProcessId(MOCK_TEST_RUNNER)
            .findFirstProcessInstance()
            .get();

    final var lastTestProcessInstance =
        InspectionUtility.findProcessInstances()
            .withBpmnProcessId(MOCK_TEST_RUNNER)
            .findLastProcessInstance()
            .get();

    assertThat(lastTestProcessInstance.getProcessInstanceKey())
        .isNotEqualTo(firstTestProcessInstance.getProcessInstanceKey());

    assertThat(firstTestProcessInstance)
        .hasVariableWithValue("channelUUID", "channel-name-uuid")
        .hasVariableWithValue("clusterPlanUUID", "clusterplan1-name-uuid")
        .hasVariableWithValue("generationUUID", "generation-name-uuid")
        .hasVariableWithValue("regionUUID", "region-name-uuid");

    assertThat(lastTestProcessInstance)
        .hasVariableWithValue("channelUUID", "channel-name-uuid")
        .hasVariableWithValue("clusterPlanUUID", "clusterplan2-name-uuid")
        .hasVariableWithValue("generationUUID", "generation-name-uuid")
        .hasVariableWithValue("regionUUID", "region-name-uuid");

    // cleanup
    jobWorkers.forEach(JobWorker::close);
  }

  @NotNull
  private ArrayList<JobWorker> registerWorkers() {
    final var jobWorkers = new ArrayList<JobWorker>();
    final var mockMapNamesToUUIDWorker =
        zeebeClient
            .newWorker()
            .jobType(Launcher.MAP_NAMES_TO_UUIDS_JOB)
            .handler(new MockMapNamesToUUIDsHandler())
            .open();
    jobWorkers.add(mockMapNamesToUUIDWorker);

    final var testReport = new TestReportDTO(TestResult.PASSED, 0, Collections.emptyList());

    final var mockTestRunnerWorker =
        zeebeClient
            .newWorker()
            .jobType("mock-test-runner-job")
            .handler(new MockTestRunnerHandler(testReport))
            .open();
    jobWorkers.add(mockTestRunnerWorker);

    final var mockDestroyZeebeClusterWorker =
        zeebeClient
            .newWorker()
            .jobType(Launcher.DESTROY_ZEEBE_CLUSTER_IN_CAMUNDA_CLOUD_JOB)
            .handler(new NoOpHandler())
            .open();
    jobWorkers.add(mockDestroyZeebeClusterWorker);

    final var realAggregateTestResultsWorker =
        zeebeClient
            .newWorker()
            .jobType(Launcher.AGGREGATE_TEST_RESULTS_JOB)
            .handler(new AggregateTestResultHandler())
            .open();
    jobWorkers.add(realAggregateTestResultsWorker);

    return jobWorkers;
  }

  private static final class MockTestRunnerHandler implements JobHandler {

    private final TestReport testReport;

    private MockTestRunnerHandler(final TestReport testReport) {
      this.testReport = testReport;
    }

    @Override
    public void handle(final JobClient client, final ActivatedJob job) {
      client
          .newCompleteCommand(job)
          .variables(Map.of(TestDriver.VARIABLE_KEY_TEST_REPORT, testReport))
          .send()
          .join();
    }
  }
}
