package io.zeebe.clustertestbench.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.community.zeebe.testutils.ZeebeWorkerAssertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.zeebe.clustertestbench.notification.NotificationService;
import io.zeebe.clustertestbench.testdriver.impl.TestReportDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NotifyEngineersHandlerTest {

  JobClientStub jobClientStub = new JobClientStub();

  ActivatedJobStub activatedJobStub;

  SimpleNotificationService simpleNotificationService;

  NotifyEngineersHandler sutNotifyEngineersHandler;

  @BeforeEach
  public void setUp() {
    simpleNotificationService = new SimpleNotificationService();

    sutNotifyEngineersHandler = new NotifyEngineersHandler(simpleNotificationService);

    activatedJobStub = createActivatedJobStub();
  }

  @Test
  public void shouldSendNotificationOverNotificationService() throws Exception {
    // when
    sutNotifyEngineersHandler.handle(jobClientStub, activatedJobStub);

    // then
    assertThat(simpleNotificationService.lastMessage)
        .contains("CLUSTER_A")
        .contains("CLUSTER_PLAN_B")
        .contains("GENERATION_C")
        .contains("https://localhost.test")
        .contains("processID")
        .contains("ERROR")
        .contains("http://jenkins/branch/build");

    assertThat(activatedJobStub).completed();
  }

  @Test
  public void shouldNotCompleteJobWhenExceptionIsThrownInNotificationService() throws Exception {
    // given
    final var notificationService = mock(NotificationService.class);
    doThrow(new Exception()).when(notificationService).sendNotification(anyString());
    final var jobClientMock = mock(JobClient.class);
    final var mock = mock(CompleteJobCommandStep1.class);
    when(jobClientMock.newCompleteCommand(anyLong())).thenReturn(mock);
    final var jobMock = createActivatedJobStub();
    final var notifyEngineersWorker = new NotifyEngineersHandler(notificationService);

    // when
    assertThatThrownBy(() -> notifyEngineersWorker.handle(jobClientMock, jobMock))
        .isInstanceOf(Exception.class);

    // then
    verify(jobClientMock, never()).newCompleteCommand(anyLong());
  }

  private ActivatedJobStub createActivatedJobStub() {
    final var activatedJobStub = jobClientStub.createActivatedJob();
    activatedJobStub.setCustomHeaders(Map.of("channel", "CHANNEL_A"));

    final var input = new HashMap<String, Object>();

    input.put("clusterName", "CLUSTER_A");
    input.put("clusterPlan", "CLUSTER_PLAN_B");
    input.put("generation", "GENERATION_C");
    input.put("operateURL", "https://localhost.test");
    input.put("testProcessId", "processID");
    input.put("businessKey", "http://jenkins/branch/build");

    final var testReport = new TestReportDTO();
    testReport.setFailureMessages(List.of("ERROR"));
    input.put("testReport", testReport);

    activatedJobStub.setInputVariables(input);
    return activatedJobStub;
  }

  private static final class SimpleNotificationService implements NotificationService {

    private String lastMessage;

    @Override
    public void sendNotification(final String message) {
      lastMessage = message;
    }
  }
}
