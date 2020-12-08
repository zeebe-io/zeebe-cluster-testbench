package io.zeebe.clustertestbench.it.handler;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.zeebe.bpmnspec.runner.zeebe.ZeebeEnvironment;
import io.zeebe.bpmnspec.runner.zeebe.zeeqs.ZeeqsClient;
import io.zeebe.bpmnspec.runner.zeebe.zeeqs.ZeeqsClient.ElementInstanceDto;
import io.zeebe.bpmnspec.runner.zeebe.zeeqs.ZeeqsClient.VariableDto;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.WorkflowInstanceEvent;
import io.zeebe.client.api.worker.JobWorker;
import io.zeebe.clustertestbench.handler.TriggerMessageStartEventHandler;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TriggerMessageStartEventHandlerIT {

  ZeebeEnvironment zeebeEnvironment = new ZeebeEnvironment();
  ZeebeClient zeebeClient;
  ZeeqsClient zeeqsClient;
  JobWorker workerRegistration;

  @BeforeEach
  void setUp() {
    zeebeEnvironment.setup();

    zeebeClient = zeebeEnvironment.getZeebeClient();
    zeeqsClient = zeebeEnvironment.getZeeqsClient();

    workerRegistration =
        zeebeClient
            .newWorker()
            .jobType("trigger-message-start-event-job")
            .handler(new TriggerMessageStartEventHandler(zeebeClient))
            .timeout(Duration.ofSeconds(10))
            .open();
  }

  @AfterEach
  void tearDown() {
    workerRegistration.close();
    zeebeEnvironment.cleanUp();
  }

  /**
   * This integration test consists of two processes:
   *
   * <ul>
   *   <lI>Main - this process contains a worker to start the secondary process via message start
   *       event
   *   <li>Secondary - this process shall be triggered by the main process
   * </ul>
   */
  @Test
  void shoulTriggerSecondaryProcess() {
    // given
    zeebeClient
        .newDeployCommand()
        .addResourceFromClasspath("it/triggermessagestartevent/main.bpmn")
        .addResourceFromClasspath("it/triggermessagestartevent/secondary.bpmn")
        .send()
        .join();

    final var variables = Map.of("key1", "value1", "key2", "value2");

    // when
    final WorkflowInstanceEvent mainWorkflowCreationResponse =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("main")
            .latestVersion()
            .variables(variables)
            .send()
            .join();

    // then
    await()
        .atMost(1, TimeUnit.MINUTES)
        .until(() -> zeeqsClient.getWorkflowInstanceKeys().size() == 2);

    final List<Long> workflowInstanceKeys = zeeqsClient.getWorkflowInstanceKeys();

    workflowInstanceKeys.remove(mainWorkflowCreationResponse.getWorkflowInstanceKey());

    final Long secondaryWorkflowInstanceKey = workflowInstanceKeys.get(0);

    final ElementInstanceDto elementDto =
        zeeqsClient.getElementInstances(secondaryWorkflowInstanceKey).get(0);

    assertThat(elementDto.getElementId()).isEqualTo("secondary");

    final List<VariableDto> variablesInSecondaryProcess =
        zeeqsClient.getWorkflowInstanceVariables(secondaryWorkflowInstanceKey);

    final Map<String, String> variablesInSecondaryProcessAsMap =
        variablesInSecondaryProcess
            .stream()
            .collect(
                toMap(
                    dto -> dto.getName(),
                    dto -> dto.getValue().replace("\"", ""))); // value is wrapped in " characters

    assertThat(variablesInSecondaryProcessAsMap).containsExactlyInAnyOrderEntriesOf(variables);
  }
}
