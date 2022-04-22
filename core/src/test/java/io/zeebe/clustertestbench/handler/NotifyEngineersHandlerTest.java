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
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.TestReportDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NotifyEngineersHandlerTest {

  public static final String CLUSTER_ID = "a1b2c3d4e-f5e6-d7c8-b9a8-b7c6d5e4f3e2";
  public static final String CLUSTER_NAME = "CLUSTER_A";
  public static final String CLUSTER_PLAN = "CLUSTER_PLAN_B";
  public static final String GENERATION = "GENERATION_C";
  public static final String IMAGE = "ZEEBE:VERSION";
  public static final String BRANCH = "BRANCH";
  public static final String OPERATE_URL = "https://localhost.test";
  public static final String PROCESS_ID = "processID";
  public static final String BUSINESS_KEY = "http://jenkins/branch/build";
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
        .contains(CLUSTER_NAME)
        .contains(CLUSTER_ID)
        .contains(CLUSTER_PLAN)
        .contains(GENERATION)
        .contains(IMAGE)
        .contains(BRANCH)
        .contains(OPERATE_URL)
        .contains(PROCESS_ID)
        .contains("ERROR")
        .contains(BUSINESS_KEY);

    System.out.println(simpleNotificationService.lastMessage);

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

    input.put("clusterName", CLUSTER_NAME);
    input.put("clusterId", CLUSTER_ID);
    input.put("clusterPlan", CLUSTER_PLAN);
    input.put("generation", GENERATION);
    input.put("zeebeImage", IMAGE);
    input.put("branch", BRANCH);
    input.put("operateURL", OPERATE_URL);
    input.put("testProcessId", PROCESS_ID);
    input.put("businessKey", BUSINESS_KEY);

    final var testReport = new TestReportDTO(TestDriver.TestResult.FAILED, 1, List.of("ERROR"));
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
