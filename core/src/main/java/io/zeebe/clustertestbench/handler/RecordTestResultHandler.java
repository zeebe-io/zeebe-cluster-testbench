package io.zeebe.clustertestbench.handler;

import static com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.impl.ZeebeObjectMapper;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.api.TestReport;
import io.zeebe.clustertestbench.testdriver.api.TestReport.TestResult;
import io.zeebe.clustertestbench.testdriver.impl.TestReportDTO;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RecordTestResultHandler implements JobHandler {

  private static final DateTimeFormatter INSTANT_FORMATTER =
      DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
          .withLocale(Locale.US)
          .withZone(ZoneId.systemDefault());

  private static final String APPLICATION_NAME =
      "Zeebe Cluster Testbench - Publish Test Results Worker";
  private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

  private static final String RANGE = "Sheet1!A1:O";

  private final String spreadSheetId;

  private final String sheetsApiKeyFileContent;

  private final Sheets service;

  public RecordTestResultHandler(String sheetsApiKeyfileContent, String spreadSheetId)
      throws IOException, GeneralSecurityException {
    super();
    this.sheetsApiKeyFileContent = sheetsApiKeyfileContent;
    this.spreadSheetId = spreadSheetId;

    try (var inputStream = new StringBufferInputStream(sheetsApiKeyFileContent)) {
      GoogleCredential credential = GoogleCredential.fromStream(inputStream).createScoped(SCOPES);

      service =
          new Sheets.Builder(
                  GoogleNetHttpTransport.newTrustedTransport(), getDefaultInstance(), credential)
              .setApplicationName(APPLICATION_NAME)
              .build();
    }
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    List<Object> rowData = buildRowDataForSheet(input);

    ValueRange body = new ValueRange().setValues(Collections.singletonList(rowData));
    service
        .spreadsheets()
        .values()
        .append(spreadSheetId, RANGE, body)
        .setValueInputOption("USER_ENTERED")
        .execute();

    client.newCompleteCommand(job.getKey()).send();
  }

  private static List<Object> buildRowDataForSheet(Input input) {
    List<Object> result = new ArrayList<>();

    result.add(input.getRegion());
    result.add(input.getChannel());
    result.add(input.getClusterPlan());
    result.add(input.getGeneration());

    result.add(input.getTestWorkflowId());

    result.add(input.getClusterName());
    result.add(input.getClusterId());
    result.add(input.getOperateURL());

    TestReport testReport = input.getTestReport();
    result.add(testReport.getTestResult().name());
    result.add(testReport.getFailureCount());
    result.add(
        returnValueIfApplicable(
            testReport, new ZeebeObjectMapper().toJson(testReport.getFailureMessages())));
    result.add(convertMillisToString(testReport.getStartTime()));
    result.add(
        returnValueIfApplicable(
            testReport, convertMillisToString(testReport.getTimeOfFirstFailure())));
    result.add(convertMillisToString(testReport.getEndTime()));
    result.add(new ZeebeObjectMapper().toJson(testReport.getMetaData()));

    return result;
  }

  private static String returnValueIfApplicable(TestReport testReport, Object value) {
    return testReport.getTestResult() == TestResult.PASSED ? "n/a" : value.toString();
  }

  private static String convertMillisToString(long millis) {
    Instant instant = Instant.ofEpochMilli(millis);

    return INSTANT_FORMATTER.format(instant);
  }

  private static final class Input {
    private String generation;
    private String region;
    private String clusterPlan;
    private String channel;

    private String testWorkflowId;

    private String clusterId;
    private String clusterName;
    private String operateURL;

    private TestReportDTO testReport;

    public String getGeneration() {
      return generation;
    }

    public void setGeneration(String generation) {
      this.generation = generation;
    }

    public String getRegion() {
      return region;
    }

    public void setRegion(String region) {
      this.region = region;
    }

    public String getClusterPlan() {
      return clusterPlan;
    }

    public void setClusterPlan(String clusterPlan) {
      this.clusterPlan = clusterPlan;
    }

    public String getChannel() {
      return channel;
    }

    public void setChannel(String channel) {
      this.channel = channel;
    }

    public String getClusterId() {
      return clusterId;
    }

    public void setClusterId(String clusterId) {
      this.clusterId = clusterId;
    }

    public String getClusterName() {
      return clusterName;
    }

    public void setClusterName(String clusterName) {
      this.clusterName = clusterName;
    }

    public String getOperateURL() {
      return operateURL;
    }

    public void setOperateURL(String operateURL) {
      this.operateURL = operateURL;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
    public TestReportDTO getTestReport() {
      return testReport;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
    public void setTestReport(TestReportDTO testReport) {
      this.testReport = testReport;
    }

    String getTestWorkflowId() {
      return testWorkflowId;
    }

    void setTestWorkflowId(String testWorkflowId) {
      this.testWorkflowId = testWorkflowId;
    }
  }
}
