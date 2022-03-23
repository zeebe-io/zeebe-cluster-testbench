package io.zeebe.clustertestbench.handler;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.community.zeebe.testutils.ZeebeWorkerAssertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.collection.Stream;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.TestResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AggregateTestResultHandlerTest {

  static Stream<Arguments> provideValidValues() {
    return Stream.of(
        Arguments.of(TestDriver.TestResult.PASSED, "PASSED", TestDriver.TestResult.PASSED),
        Arguments.of(TestDriver.TestResult.PASSED, "SKIPPED", TestDriver.TestResult.PASSED),
        Arguments.of(TestDriver.TestResult.PASSED, "FAILED", TestDriver.TestResult.FAILED),
        Arguments.of(TestDriver.TestResult.SKIPPED, "PASSED", TestDriver.TestResult.PASSED),
        Arguments.of(TestDriver.TestResult.SKIPPED, "SKIPPED", TestDriver.TestResult.SKIPPED),
        Arguments.of(TestDriver.TestResult.SKIPPED, "FAILED", TestDriver.TestResult.FAILED),
        Arguments.of(TestDriver.TestResult.FAILED, "PASSED", TestDriver.TestResult.FAILED),
        Arguments.of(TestDriver.TestResult.FAILED, "SKIPPED", TestDriver.TestResult.FAILED),
        Arguments.of(TestDriver.TestResult.FAILED, "FAILED", TestDriver.TestResult.FAILED),
        Arguments.of(
            TestDriver.TestResult.SKIPPED,
            List.of("SKIPPED", "SKIPPED"),
            TestDriver.TestResult.SKIPPED),
        Arguments.of(
            TestDriver.TestResult.SKIPPED,
            List.of("SKIPPED", "PASSED"),
            TestDriver.TestResult.PASSED),
        Arguments.of(
            TestDriver.TestResult.SKIPPED,
            List.of("SKIPPED", "FAILED"),
            TestDriver.TestResult.FAILED),
        Arguments.of(
            TestDriver.TestResult.SKIPPED,
            List.of("PASSED", "PASSED"),
            TestDriver.TestResult.PASSED),
        Arguments.of(
            TestDriver.TestResult.SKIPPED,
            List.of("PASSED", "FAILED"),
            TestDriver.TestResult.FAILED),
        Arguments.of(
            TestDriver.TestResult.SKIPPED,
            List.of("FAILED", "FAILED"),
            TestDriver.TestResult.FAILED),
        Arguments.of(
            TestDriver.TestResult.SKIPPED,
            List.of("PASSED", "SKIPPED", "PASSED", "FAILED", "PASSED", "PASSED"),
            TestDriver.TestResult.FAILED),
        Arguments.of(
            TestDriver.TestResult.SKIPPED,
            List.of("PASSED", "SKIPPED", "PASSED", "SKIPPED", "PASSED", "PASSED"),
            TestDriver.TestResult.PASSED),
        Arguments.of(
            TestDriver.TestResult.SKIPPED, Collections.EMPTY_LIST, TestDriver.TestResult.SKIPPED),
        Arguments.of(
            TestDriver.TestResult.PASSED, Collections.EMPTY_LIST, TestDriver.TestResult.PASSED),
        Arguments.of(
            TestDriver.TestResult.FAILED, Collections.EMPTY_LIST, TestDriver.TestResult.FAILED));
  }

  static Stream<Arguments> provideInvalidValues() {
    return Stream.of(
        Arguments.of(""),
        Arguments.of("dummy"),
        Arguments.of(123),
        Arguments.of(new Object()),
        Arguments.of(List.of(123, "SKIPPED")));
  }

  @Nested
  public class HandleJobTest {
    JobClientStub jobClientStub = new JobClientStub();

    ActivatedJobStub activatedJobStub;

    final AggregateTestResultHandler sutHandler = new AggregateTestResultHandler();

    @BeforeEach
    public void setUp() {
      activatedJobStub = jobClientStub.createActivatedJob();
    }

    @Test
    public void shouldAggregateResultOfDifferentTestsWhichStoreTheirResultInDifferentVariables()
        throws Exception {
      // given
      activatedJobStub.setCustomHeaders(
          Map.of(AggregateTestResultHandler.KEY_VARAIBLENAMES, "testA_result, testB_result"));
      activatedJobStub.setInputVariables(
          Map.of("testA_result", "PASSED", "testB_result", "FAILED"));

      // when
      sutHandler.handle(jobClientStub, activatedJobStub);

      // then
      assertThat(activatedJobStub)
          .completed()
          .extractingOutput()
          .containsExactly(entry(AggregateTestResultHandler.KEY_AGGREGATED_RESULT, "FAILED"));
    }

    @Test
    public void shouldAggregateResultsOfTestsThatRunAsMultiInstanceAndStoreTheirResultInAnArray()
        throws Exception {
      // given

      activatedJobStub.setCustomHeaders(
          Map.of(AggregateTestResultHandler.KEY_VARAIBLENAMES, "testResults"));
      activatedJobStub.setInputVariables(
          Map.of("testResults", List.of("PASSED", "SKIPPED", "FAILED", "PASSED")));

      // when
      sutHandler.handle(jobClientStub, activatedJobStub);

      assertThat(activatedJobStub)
          .completed()
          .extractingOutput()
          .containsExactly(entry(AggregateTestResultHandler.KEY_AGGREGATED_RESULT, "FAILED"));
    }

    @Test
    public void shouldReturnSkippedWhenAggregatingAnEmptyList() throws Exception {
      // given

      activatedJobStub.setCustomHeaders(
          Map.of(AggregateTestResultHandler.KEY_VARAIBLENAMES, "testResults"));
      activatedJobStub.setInputVariables(Map.of("testResults", Collections.EMPTY_LIST));

      // when
      sutHandler.handle(jobClientStub, activatedJobStub);

      assertThat(activatedJobStub)
          .completed()
          .extractingOutput()
          .containsExactly(entry(AggregateTestResultHandler.KEY_AGGREGATED_RESULT, "SKIPPED"));
    }
  }

  @Nested
  public class AddToAggregateTest {
    final AggregateTestResultHandler sutHandler = new AggregateTestResultHandler();

    @ParameterizedTest
    @MethodSource(
        "io.zeebe.clustertestbench.handler.AggregateTestResultHandlerTest#provideValidValues")
    void shouldAggregateValidValuesCorrectly(
        final TestResult currentAggregate, final Object valueToAdd, final TestResult expected)
        throws JsonProcessingException {
      // when
      final var actual = sutHandler.addToAggregate(currentAggregate, valueToAdd);

      // then
      assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource(
        "io.zeebe.clustertestbench.handler.AggregateTestResultHandlerTest#provideInvalidValues")
    void shouldRejectInvalidValues(final Object valueToAdd) throws JsonProcessingException {

      // when + then
      assertThatThrownBy(() -> sutHandler.addToAggregate(TestDriver.TestResult.SKIPPED, valueToAdd))
          .isInstanceOf(Exception.class);
    }

    @Test
    void shouldRejectNull() throws JsonProcessingException {
      // when + then
      assertThatThrownBy(() -> sutHandler.addToAggregate(TestDriver.TestResult.SKIPPED, null))
          .isInstanceOf(Exception.class);
    }
  }
}
