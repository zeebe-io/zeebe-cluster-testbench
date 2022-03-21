package io.zeebe.clustertestbench.testdriver.impl;

import io.zeebe.clustertestbench.testdriver.api.TestReport;
import java.util.List;
import java.util.Map;

public class TestReportDTO implements TestReport {

  private TestResult testResult;
  private List<String> failureMessages;
  private int failureCount;
  private Map<String, Object> metaData;
  private long startTime;
  private long endTime;
  private long timeOfFirstFailure;

  @Override
  public TestResult getTestResult() {
    return testResult;
  }

  @Override
  public List<String> getFailureMessages() {
    return failureMessages;
  }

  public void setFailureMessages(final List<String> failureMessages) {
    this.failureMessages = failureMessages;
  }

  public void setTestResult(final TestResult testResult) {
    this.testResult = testResult;
  }

  public int getFailureCount() {
    return failureCount;
  }

  public void setFailureCount(final int failureCount) {
    this.failureCount = failureCount;
  }

  public Map<String, Object> getMetaData() {
    return metaData;
  }

  public void setMetaData(final Map<String, Object> metaData) {
    this.metaData = metaData;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(final long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(final long endTime) {
    this.endTime = endTime;
  }

  public long getTimeOfFirstFailure() {
    return timeOfFirstFailure;
  }

  public void setTimeOfFirstFailure(final long timeOfFirstFailure) {
    this.timeOfFirstFailure = timeOfFirstFailure;
  }
}
