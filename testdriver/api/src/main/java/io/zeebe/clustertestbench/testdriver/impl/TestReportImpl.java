package io.zeebe.clustertestbench.testdriver.impl;

import io.camunda.zeebe.client.impl.ZeebeObjectMapper;
import io.zeebe.clustertestbench.testdriver.api.TestReport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestReportImpl implements TestReport, AutoCloseable {

  private TestResult testResult = TestResult.PASSED;

  private final long startTime;
  private long endTime;
  private long timeOfFirstFailure;

  private int failureCount = 0;

  private final List<String> failureMessages = new ArrayList<>();

  private final Map<String, Object> metaData;

  public TestReportImpl(final Map<String, Object> metaData) {
    startTime = System.currentTimeMillis();
    this.metaData = new HashMap<>(metaData);
  }

  @Override
  public void close() {
    endTime = System.currentTimeMillis();
  }

  public synchronized void addFailure(final String failureMessage) {
    testResult = TestResult.FAILED;
    if (failureCount == 0) {
      timeOfFirstFailure = System.currentTimeMillis();
    }
    failureMessages.add(failureMessage);
    failureCount++;
  }

  @Override
  public TestResult getTestResult() {
    return testResult;
  }

  @Override
  public List<String> getFailureMessages() {
    return Collections.unmodifiableList(failureMessages);
  }

  @Override
  public int getFailureCount() {
    return failureCount;
  }

  @Override
  public Map<String, Object> getMetaData() {
    return Collections.unmodifiableMap(metaData);
  }

  @Override
  public long getStartTime() {
    return startTime;
  }

  @Override
  public long getEndTime() {
    return endTime;
  }

  @Override
  public long getTimeOfFirstFailure() {
    return timeOfFirstFailure;
  }

  @Override
  public String toString() {
    return new ZeebeObjectMapper().toJson(this);
  }
}
