package io.zeebe.clustertestbench.testdriver.api;

import java.util.List;
import java.util.Map;

public interface TestReport {

  public enum TestResult {
    PASSED,
    FAILED,
    SKIPPED // currently used by the chaos experiments
  }

  TestResult getTestResult();

  List<String> getFailureMessages();

  int getFailureCount();

  Map<String, Object> getMetaData();

  long getStartTime();

  long getEndTime();

  default long getDuration() {
    return getEndTime() - getStartTime();
  }

  long getTimeOfFirstFailure();
}
