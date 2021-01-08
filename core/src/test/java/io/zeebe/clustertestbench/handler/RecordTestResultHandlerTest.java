package io.zeebe.clustertestbench.handler;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.zeebe.clustertestbench.handler.RecordTestResultHandler.Input;
import io.zeebe.clustertestbench.testdriver.api.TestReport.TestResult;
import io.zeebe.clustertestbench.testdriver.impl.TestReportDTO;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class RecordTestResultHandlerTest {

  @Test
  void testShouldNotWriteNullsIntoRowData() {
    // given
    final var input = createTestInput();

    // when
    final var actual = RecordTestResultHandler.buildRowDataForSheet(input);

    // then
    assertThat(actual).doesNotContainNull();
  }

  @ParameterizedTest
  @MethodSource("getColumsAndSetters")
  void testShouldWriteValueIntoCorrectColumn(
      Tuple3<String, Integer, BiConsumer<Input, String>> columnIndexAndSetter) {
    // given
    final var columnIndex = columnIndexAndSetter._2();
    final var setter = columnIndexAndSetter._3();

    final var input = createTestInput();

    final var value = "example value";

    setter.accept(input, value);

    // when
    final var actual = RecordTestResultHandler.buildRowDataForSheet(input);

    // then
    assertThat(actual.get(columnIndex)).isEqualTo(value);
  }

  @ParameterizedTest
  @MethodSource("getColumsAndSetters")
  void testShouldReplaceNullValueWithNA(
      Tuple3<String, Integer, BiConsumer<Input, String>> columnIndexAndSetter) {
    // given
    final var columnIndex = columnIndexAndSetter._2();
    final var setter = columnIndexAndSetter._3();

    final var input = createTestInput();

    setter.accept(input, null);

    // when
    final var actual = RecordTestResultHandler.buildRowDataForSheet(input);

    // then
    assertThat(actual.get(columnIndex)).isEqualTo("n/a");
  }

  static Stream<Tuple3<String, Integer, BiConsumer<Input, String>>> getColumsAndSetters() {
    /*
     * the string part of the tuple is to have something human readable in test case failure messages
     */
    return Stream.of(
        Tuple.of("Region", 0, Input::setRegion),
        Tuple.of("Channel", 1, Input::setChannel),
        Tuple.of("Cluster Plan", 2, Input::setClusterPlan),
        Tuple.of("Generation", 3, Input::setGeneration),
        Tuple.of("Business Key", 4, Input::setBusinessKey),
        Tuple.of("Test", 5, Input::setTestWorkflowId),
        Tuple.of("Cluster Name", 6, Input::setClusterName),
        Tuple.of("Cluster ID", 7, Input::setClusterId),
        Tuple.of("Operate URL", 8, Input::setOperateURL));
  }

  private Input createTestInput() {
    final var result = new Input();

    final var testReport = new TestReportDTO();

    testReport.setTestResult(TestResult.PASSED);

    result.setTestReport(testReport);
    return result;
  }
}
