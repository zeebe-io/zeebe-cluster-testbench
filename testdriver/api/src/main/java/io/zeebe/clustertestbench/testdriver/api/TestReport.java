package io.zeebe.clustertestbench.testdriver.api;

import java.util.List;
import java.util.Map;

public interface TestReport {

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

  public enum TestResult {
    PASSED,
    FAILED,
    SKIPPED; // currently used by the chaos experiments

    public static TestResult aggregate(TestResult input1, TestResult input2) {
      if ((input1 == FAILED) || (input2 == FAILED)) {
        return FAILED;
      } else if ((input1 == PASSED) || (input2 == PASSED)) {
        return PASSED;
      } else {
        return SKIPPED;
      }
    }
  }
}
