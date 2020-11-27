package io.zeebe.clustertestbench.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.zeebe.client.api.command.CompleteJobCommandStep1;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.clustertestbench.handler.NotifyEngineersHandler.Input;
import io.zeebe.clustertestbench.notification.NotificationService;
import io.zeebe.clustertestbench.testdriver.impl.TestReportDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class NotifyEngineersHandlerTest {

  @Test
  public void shouldSendNotificationOverNotificationService() throws Exception {
    // given
    final var simpleNotificationService = new SimpleNotificationService();
    final var jobClientMock = mock(JobClient.class);
    final var mock = mock(CompleteJobCommandStep1.class);
    when(jobClientMock.newCompleteCommand(anyLong())).thenReturn(mock);
    final var jobMock = createJobMock();
    final var notifyEngineersWorker = new NotifyEngineersHandler(simpleNotificationService);

    // when
    notifyEngineersWorker.handle(jobClientMock, jobMock);

    // then
    verify(jobClientMock).newCompleteCommand(0xCAFEL);
    assertThat(simpleNotificationService.lastMessage)
        .contains("CLUSTER_A")
        .contains("CLUSTER_PLAN_B")
        .contains("GENERATION_C")
        .contains("https://localhost.test")
        .contains("workflowID")
        .contains("ERROR");
  }

  @Test
  public void shouldNotCompleteJobWhenExceptionIsThrownInNotificationService() throws Exception {
    // given
    final var notificationService = mock(NotificationService.class);
    doThrow(new Exception()).when(notificationService).sendNotification(anyString());
    final var jobClientMock = mock(JobClient.class);
    final var mock = mock(CompleteJobCommandStep1.class);
    when(jobClientMock.newCompleteCommand(anyLong())).thenReturn(mock);
    final var jobMock = createJobMock();
    final var notifyEngineersWorker = new NotifyEngineersHandler(notificationService);

    // when
    assertThatThrownBy(() -> notifyEngineersWorker.handle(jobClientMock, jobMock))
        .isInstanceOf(Exception.class);

    // then
    verify(jobClientMock, never()).newCompleteCommand(anyLong());
  }

  private ActivatedJob createJobMock() {
    final var jobMock = mock(ActivatedJob.class);
    when(jobMock.getCustomHeaders()).thenReturn(Map.of("channel", "CHANNEL_A"));
    final var input = new Input();
    input.setClusterName("CLUSTER_A");
    input.setClusterPlan("CLUSTER_PLAN_B");
    input.setGeneration("GENERATION_C");
    input.setOperateURL("https://localhost.test");
    input.setTestWorkflowId("workflowID");
    final var testReport = new TestReportDTO();
    testReport.setFailureMessages(List.of("ERROR"));
    input.setTestReport(testReport);
    when(jobMock.getVariablesAsType(Input.class)).thenReturn(input);
    when(jobMock.getKey()).thenReturn(0xCAFEL);
    return jobMock;
  }

  private static final class SimpleNotificationService implements NotificationService {

    private String lastMessage;

    @Override
    public void sendNotification(final String message) throws Exception {
      lastMessage = message;
    }
  }
}
