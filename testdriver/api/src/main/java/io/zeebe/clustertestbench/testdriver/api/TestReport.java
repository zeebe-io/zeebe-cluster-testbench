package io.zeebe.clustertestbench.testdriver.api;

import java.util.List;

public interface TestReport {

  TestResult testResult();

  List<String> failureMessages();

  int failureCount();

  record TestReportDTO(TestResult testResult, int failureCount, List<String> failureMessages)
      implements TestReport {}

  enum TestResult {
    PASSED,
    FAILED,
    SKIPPED; // currently used by the chaos experiments

    public static TestResult aggregate(final TestResult input1, final TestResult input2) {
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
