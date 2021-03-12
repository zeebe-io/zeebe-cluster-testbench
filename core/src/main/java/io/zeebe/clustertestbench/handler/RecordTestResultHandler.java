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

  private static final String NA = "n/a";

  private static final DateTimeFormatter INSTANT_FORMATTER =
      DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
          .withLocale(Locale.US)
          .withZone(ZoneId.systemDefault());

  private static final String APPLICATION_NAME =
      "Zeebe Cluster Testbench - Publish Test Results Worker";
  private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

  private static final String RANGE = "Sheet1!A1:P";

  private final String spreadSheetId;

  private final String sheetsApiKeyFileContent;

  private final Sheets service;

  public RecordTestResultHandler(final String sheetsApiKeyfileContent, final String spreadSheetId)
      throws IOException, GeneralSecurityException {
    super();
    this.sheetsApiKeyFileContent = sheetsApiKeyfileContent;
    this.spreadSheetId = spreadSheetId;

    try (final var inputStream = new StringBufferInputStream(sheetsApiKeyFileContent)) {
      final GoogleCredential credential =
          GoogleCredential.fromStream(inputStream).createScoped(SCOPES);

      service =
          new Sheets.Builder(
                  GoogleNetHttpTransport.newTrustedTransport(), getDefaultInstance(), credential)
              .setApplicationName(APPLICATION_NAME)
              .build();
    }
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final Input input = job.getVariablesAsType(Input.class);

    final List<Object> rowData = buildRowDataForSheet(input);

    final ValueRange body = new ValueRange().setValues(Collections.singletonList(rowData));
    service
        .spreadsheets()
        .values()
        .append(spreadSheetId, RANGE, body)
        .setValueInputOption("USER_ENTERED")
        .execute();

    client.newCompleteCommand(job.getKey()).send();
  }

  protected static List<Object> buildRowDataForSheet(final Input input) {
    final List<Object> result = new ArrayList<>();

    result.add(input.getRegion());
    result.add(input.getChannel());
    result.add(input.getClusterPlan());
    result.add(input.getGeneration());
    result.add(input.getBusinessKey());

    result.add(input.getTestProcessId());

    result.add(input.getClusterName());
    result.add(input.getClusterId());
    result.add(input.getOperateURL());

    final TestReport testReport = input.getTestReport();
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

    result.replaceAll(item -> item == null ? NA : item);

    return result;
  }

  private static String returnValueIfApplicable(final TestReport testReport, final Object value) {
    return testReport.getTestResult() == TestResult.PASSED ? NA : value.toString();
  }

  private static String convertMillisToString(final long millis) {
    final Instant instant = Instant.ofEpochMilli(millis);

    return INSTANT_FORMATTER.format(instant);
  }

  protected static final class Input {
    private String generation;
    private String region;
    private String clusterPlan;
    private String channel;
    private String businessKey;

    private String testProcessId;

    private String clusterId;
    private String clusterName;
    private String operateURL;

    private TestReportDTO testReport;

    public String getGeneration() {
      return generation;
    }

    public void setGeneration(final String generation) {
      this.generation = generation;
    }

    public String getRegion() {
      return region;
    }

    public void setRegion(final String region) {
      this.region = region;
    }

    public String getClusterPlan() {
      return clusterPlan;
    }

    public void setClusterPlan(final String clusterPlan) {
      this.clusterPlan = clusterPlan;
    }

    public String getChannel() {
      return channel;
    }

    public void setChannel(final String channel) {
      this.channel = channel;
    }

    public String getBusinessKey() {
      return businessKey;
    }

    public void setBusinessKey(String businessKey) {
      this.businessKey = businessKey;
    }

    public String getClusterId() {
      return clusterId;
    }

    public void setClusterId(final String clusterId) {
      this.clusterId = clusterId;
    }

    public String getClusterName() {
      return clusterName;
    }

    public void setClusterName(final String clusterName) {
      this.clusterName = clusterName;
    }

    public String getOperateURL() {
      return operateURL;
    }

    public void setOperateURL(final String operateURL) {
      this.operateURL = operateURL;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
    public TestReportDTO getTestReport() {
      return testReport;
    }

    @JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
    public void setTestReport(final TestReportDTO testReport) {
      this.testReport = testReport;
    }

    String getTestProcessId() {
      return testProcessId;
    }

    void setTestProcessId(final String testProcessId) {
      this.testProcessId = testProcessId;
    }
  }
}
