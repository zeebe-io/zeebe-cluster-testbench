package io.zeebe.clustertestbench.testdriver.api;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.test.util.JsonUtil;
import io.zeebe.clustertestbench.testdriver.api.TestReport.TestReportDTO;
import io.zeebe.clustertestbench.testdriver.api.TestReport.TestResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TestReportTest {

  private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

  @Test
  void testSerializationForTestReport() throws JsonProcessingException {
    // given
    final var testReport =
        new TestReport.TestReportDTO(TestResult.FAILED, 2, of("failure1", "failure2"));

    // when
    final var jsonRepresentation = OBJECT_MAPPER.writeValueAsString(testReport);

    // then
    JsonUtil.assertEquality(
        jsonRepresentation,
        """
                {
                   "testResult":"FAILED",
                   "failureCount":2,
                   "failureMessages":["failure1","failure2"]
                }
              """);
  }

  @Test
  void testDeserializationForTestReportWithAdditionalProperties() throws JsonProcessingException {
    // given
    final var jsonRepresentation =
        """
           {
              "testResult":"FAILED",
              "failureCount":2,
              "failureMessages":["failure1","failure2"],
              "additionalProperty":"lorem ipsum"
           }
         """;
    final var expectedTestReport =
        new TestReport.TestReportDTO(TestResult.FAILED, 2, of("failure1", "failure2"));

    // when
    final var actualTestReport = OBJECT_MAPPER.readValue(jsonRepresentation, TestReportDTO.class);

    // then
    Assertions.assertThat(actualTestReport).isEqualTo(expectedTestReport);
  }
}
