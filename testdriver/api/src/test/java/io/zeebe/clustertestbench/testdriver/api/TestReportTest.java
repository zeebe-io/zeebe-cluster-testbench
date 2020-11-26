package io.zeebe.clustertestbench.testdriver.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.zeebe.clustertestbench.testdriver.api.TestReport.TestResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TestReportTest {

  @ParameterizedTest
  @CsvSource({
    "PASSED, PASSED, PASSED",
    "PASSED, SKIPPED, PASSED",
    "PASSED, FAILED, FAILED",
    "SKIPPED, PASSED, PASSED",
    "SKIPPED, SKIPPED, SKIPPED",
    "SKIPPED, FAILED, FAILED",
    "FAILED, PASSED, FAILED",
    "FAILED, SKIPPED, FAILED",
    "FAILED, FAILED, FAILED"
  })
  void testTestResultAggregation(
      final TestResult input1, final TestResult input2, final TestResult expected) {

    // when
    final var actual = TestResult.aggregate(input1, input2);

    // then
    assertThat(actual).isEqualTo(expected);
  }
}
