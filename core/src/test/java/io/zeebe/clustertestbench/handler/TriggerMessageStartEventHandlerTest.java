package io.zeebe.clustertestbench.handler;

import static io.zeebe.clustertestbench.handler.TriggerMessageStartEventHandler.KEY_MESSAGE_NAME;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.community.zeebe.testutils.ZeebeWorkerAssertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep2;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep3;
import java.util.HashMap;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TriggerMessageStartEventHandlerTest {
  private static final Long TEST_JOB_KEY = 42L;

  JobClientStub jobClientStub = new JobClientStub();

  ActivatedJobStub activatedJobStub;

  TriggerMessageStartEventHandler sutHandler;

  @Mock ZeebeClient mockZeebeClient;

  @Mock PublishMessageCommandStep1 mockPublishMessageCommandStep1;
  @Mock PublishMessageCommandStep2 mockPublishMessageCommandStep2;
  @Mock PublishMessageCommandStep3 mockPublishMessageCommandStep3;

  @Mock
  ZeebeFuture<io.camunda.zeebe.client.api.response.PublishMessageResponse>
      mockZeebeFuturePublishMessageCommand;

  @BeforeEach
  void setUo() {
    sutHandler = new TriggerMessageStartEventHandler(mockZeebeClient);
    activatedJobStub = jobClientStub.createActivatedJob();
  }

  @Test
  void shouldRejectNullInConstructor() {
    // when + then
    assertThatThrownBy(() -> new TriggerMessageStartEventHandler(null))
        .isExactlyInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowExceptionIfHeaderIsMissing() {
    // given
    activatedJobStub.setCustomHeaders(emptyMap());

    // when + then
    assertThatThrownBy(() -> sutHandler.handle(jobClientStub, activatedJobStub))
        .isExactlyInstanceOf(IllegalArgumentException.class)
        .hasMessage("Header value 'messageName' is not defined");

    assertThat(activatedJobStub).isStillActivated();
  }

  @Test
  void shouldThrowExceptionIfHeaderEntryIsEmpty() {
    // given
    activatedJobStub.setCustomHeaders(singletonMap(KEY_MESSAGE_NAME, ""));

    // when + then
    assertThatThrownBy(() -> sutHandler.handle(jobClientStub, activatedJobStub))
        .isExactlyInstanceOf(IllegalArgumentException.class);

    assertThat(activatedJobStub).isStillActivated();
  }

  @Test
  void shouldSendMessageWithVariablesFromJobAndCompleteJob() throws Exception {
    // given
    final var messageName = "testMessageName";
    activatedJobStub.setCustomHeaders(singletonMap(KEY_MESSAGE_NAME, messageName));

    final var variables = new HashMap<String, Object>(singletonMap("testKey", "testValue"));
    variables.put("testKey", "testValue");
    variables.put("anotherKey", "anotherValue");
    activatedJobStub.setInputVariables(variables);

    when(mockZeebeClient.newPublishMessageCommand()).thenReturn(mockPublishMessageCommandStep1);
    when(mockPublishMessageCommandStep1.messageName(Mockito.anyString()))
        .thenReturn(mockPublishMessageCommandStep2);
    when(mockPublishMessageCommandStep2.correlationKey(Mockito.anyString()))
        .thenReturn(mockPublishMessageCommandStep3);
    when(mockPublishMessageCommandStep3.variables(Mockito.anyString()))
        .thenReturn(mockPublishMessageCommandStep3);
    when(mockPublishMessageCommandStep3.send()).thenReturn(mockZeebeFuturePublishMessageCommand);

    // when
    sutHandler.handle(jobClientStub, activatedJobStub);

    // then

    // should send message
    verify(mockZeebeClient).newPublishMessageCommand();
    verify(mockPublishMessageCommandStep1).messageName(messageName);
    verify(mockPublishMessageCommandStep3).variables(activatedJobStub.getVariables());
    verify(mockPublishMessageCommandStep3).send();
    verify(mockZeebeFuturePublishMessageCommand).join();

    // should complete job
    assertThat(activatedJobStub).completed();
  }
}
