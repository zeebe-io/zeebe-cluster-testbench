package io.zeebe.clustertestbench.handler;

import static io.zeebe.clustertestbench.handler.TriggerMessageStartEventHandler.KEY_MESSAGE_NAME;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.ZeebeFuture;
import io.zeebe.client.api.command.CompleteJobCommandStep1;
import io.zeebe.client.api.command.PublishMessageCommandStep1;
import io.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep2;
import io.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep3;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TriggerMessageStartEventHandlerTest {
  private static final Long TEST_JOB_KEY = 42L;

  @Mock JobClient mockJobClient;

  @Mock PublishMessageCommandStep1 mockPublishMessageCommandStep1;
  @Mock PublishMessageCommandStep2 mockPublishMessageCommandStep2;
  @Mock PublishMessageCommandStep3 mockPublishMessageCommandStep3;

  @SuppressWarnings("rawtypes")
  @Mock
  ZeebeFuture mockZeebeFuturePublishMessageCommand;

  @Mock CompleteJobCommandStep1 mockCompleteJobCommandStep1;

  @SuppressWarnings("rawtypes")
  @Mock
  ZeebeFuture mockZeebeFutureConpleteJobCommand;

  @Mock ActivatedJob mockActivatedJob;

  @Mock ZeebeClient mockZeebeClient;

  TriggerMessageStartEventHandler sutHandler;

  @BeforeEach
  void setUo() {
    sutHandler = new TriggerMessageStartEventHandler(mockZeebeClient);
  }

  @Test
  void shouldRejectNullInConstructor() {
    // when + then
    assertThatThrownBy(() -> new TriggerMessageStartEventHandler(null))
        .isExactlyInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowExceptionIfHeaderIsMissing() throws Exception {
    // given
    when(mockActivatedJob.getCustomHeaders()).thenReturn(emptyMap());

    // when + then
    assertThatThrownBy(() -> sutHandler.handle(mockJobClient, mockActivatedJob))
        .isExactlyInstanceOf(IllegalArgumentException.class)
        .hasMessage("Header value 'messageName' is not defined");
  }

  @Test
  void shouldThrowExceptionIfHeaderEntryIsEmpty() {
    // given
    when(mockActivatedJob.getCustomHeaders()).thenReturn(singletonMap(KEY_MESSAGE_NAME, ""));

    // when + then
    assertThatThrownBy(() -> sutHandler.handle(mockJobClient, mockActivatedJob))
        .isExactlyInstanceOf(IllegalArgumentException.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldSendMessageWithVariablesFromJobAndCompleteJob() throws Exception {
    // given
    final var messageName = "testMessageName";
    final var variables = "testVariablesString";

    mockJobCompletChain();

    when(mockActivatedJob.getCustomHeaders())
        .thenReturn(singletonMap(KEY_MESSAGE_NAME, messageName));

    when(mockActivatedJob.getVariables()).thenReturn(variables);

    when(mockZeebeClient.newPublishMessageCommand()).thenReturn(mockPublishMessageCommandStep1);
    when(mockPublishMessageCommandStep1.messageName(Mockito.anyString()))
        .thenReturn(mockPublishMessageCommandStep2);
    when(mockPublishMessageCommandStep2.correlationKey(Mockito.anyString()))
        .thenReturn(mockPublishMessageCommandStep3);
    when(mockPublishMessageCommandStep3.variables(Mockito.anyString()))
        .thenReturn(mockPublishMessageCommandStep3);
    when(mockPublishMessageCommandStep3.send()).thenReturn(mockZeebeFuturePublishMessageCommand);

    // when
    sutHandler.handle(mockJobClient, mockActivatedJob);

    // then

    // should send message
    verify(mockZeebeClient).newPublishMessageCommand();
    verify(mockPublishMessageCommandStep1).messageName(messageName);
    verify(mockPublishMessageCommandStep3).variables(variables);
    verify(mockPublishMessageCommandStep3).send();
    verify(mockZeebeFuturePublishMessageCommand).join();

    // should complete job
    verify(mockJobClient).newCompleteCommand(TEST_JOB_KEY);
    verify(mockCompleteJobCommandStep1).send();
    verify(mockZeebeFutureConpleteJobCommand).join();
  }

  @SuppressWarnings("unchecked")
  private void mockJobCompletChain() {
    when(mockActivatedJob.getKey()).thenReturn(TEST_JOB_KEY);
    when(mockJobClient.newCompleteCommand(Mockito.anyLong()))
        .thenReturn(mockCompleteJobCommandStep1);
    when(mockCompleteJobCommandStep1.send()).thenReturn(mockZeebeFutureConpleteJobCommand);
  }
}
