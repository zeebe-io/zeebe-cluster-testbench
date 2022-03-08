package io.zeebe.clustertestbench.it.handler;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.process.test.extension.testcontainer.ZeebeProcessTest;
import io.camunda.zeebe.process.test.inspections.InspectionUtility;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.zeebe.clustertestbench.handler.TriggerMessageStartEventHandler;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@ZeebeProcessTest
public class TriggerMessageStartEventHandlerIT {

  private ZeebeClient zeebeClient;

  private JobWorker workerRegistration;

  @BeforeEach
  void setUp() {
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
  void shouldTriggerSecondaryProcess() throws InterruptedException {
    // given
    final DeploymentEvent deploymentEvent =
        zeebeClient
            .newDeployCommand()
            .addResourceFromClasspath("it/triggermessagestartevent/main.bpmn")
            .addResourceFromClasspath("it/triggermessagestartevent/secondary.bpmn")
            .send()
            .join();

    final var variables = Map.of("key1", "value1", "key2", "value2");

    // when
    final ProcessInstanceEvent mainProcessCreationResponse =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("main")
            .latestVersion()
            .variables(variables)
            .send()
            .join();

    // TODO replace with waitForIdleState
    Thread.sleep(250);

    // then
    assertThat(deploymentEvent)
        .extractingProcessByResourceName("it/triggermessagestartevent/secondary.bpmn")
        .hasInstances(1);

    final Optional<InspectedProcessInstance> optProcessInstance =
        InspectionUtility.findProcessEvents().findLastProcessInstance();

    assertThat(optProcessInstance).isPresent();

    final InspectedProcessInstance lastProcessInstance = optProcessInstance.get();

    assertThat(lastProcessInstance).hasVariableWithValue("key1", "value1");
    assertThat(lastProcessInstance).hasVariableWithValue("key2", "value2");
  }
}
