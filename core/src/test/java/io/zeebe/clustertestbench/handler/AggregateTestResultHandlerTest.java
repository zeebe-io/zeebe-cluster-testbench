package io.zeebe.clustertestbench.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.vavr.collection.Stream;
import io.zeebe.clustertestbench.testdriver.api.TestReport.TestResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AggregateTestResultHandlerTest {

  static Stream<Arguments> provideValidValues() {
    return Stream.of(
        Arguments.of(TestResult.PASSED, "PASSED", TestResult.PASSED),
        Arguments.of(TestResult.PASSED, "SKIPPED", TestResult.PASSED),
        Arguments.of(TestResult.PASSED, "FAILED", TestResult.FAILED),
        Arguments.of(TestResult.SKIPPED, "PASSED", TestResult.PASSED),
        Arguments.of(TestResult.SKIPPED, "SKIPPED", TestResult.SKIPPED),
        Arguments.of(TestResult.SKIPPED, "FAILED", TestResult.FAILED),
        Arguments.of(TestResult.FAILED, "PASSED", TestResult.FAILED),
        Arguments.of(TestResult.FAILED, "SKIPPED", TestResult.FAILED),
        Arguments.of(TestResult.FAILED, "FAILED", TestResult.FAILED),
        Arguments.of(TestResult.SKIPPED, List.of("SKIPPED", "SKIPPED"), TestResult.SKIPPED),
        Arguments.of(TestResult.SKIPPED, List.of("SKIPPED", "PASSED"), TestResult.PASSED),
        Arguments.of(TestResult.SKIPPED, List.of("SKIPPED", "FAILED"), TestResult.FAILED),
        Arguments.of(TestResult.SKIPPED, List.of("PASSED", "PASSED"), TestResult.PASSED),
        Arguments.of(TestResult.SKIPPED, List.of("PASSED", "FAILED"), TestResult.FAILED),
        Arguments.of(TestResult.SKIPPED, List.of("FAILED", "FAILED"), TestResult.FAILED),
        Arguments.of(
            TestResult.SKIPPED,
            List.of("PASSED", "SKIPPED", "PASSED", "FAILED", "PASSED", "PASSED"),
            TestResult.FAILED),
        Arguments.of(
            TestResult.SKIPPED,
            List.of("PASSED", "SKIPPED", "PASSED", "SKIPPED", "PASSED", "PASSED"),
            TestResult.PASSED),
        Arguments.of(TestResult.SKIPPED, Collections.EMPTY_LIST, TestResult.SKIPPED),
        Arguments.of(TestResult.PASSED, Collections.EMPTY_LIST, TestResult.PASSED),
        Arguments.of(TestResult.FAILED, Collections.EMPTY_LIST, TestResult.FAILED));
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
  @ExtendWith(MockitoExtension.class)
  public class HandleJobTest {
    @Mock JobClient mockJobClient;

    @Mock CompleteJobCommandStep1 mockCompleteJobCommandStep1;
    @Mock CompleteJobCommandStep1 mockCompleteJobCommandStep2;

    @SuppressWarnings("rawtypes")
    @Mock
    ZeebeFuture mockZeebeFuture;

    @Mock ActivatedJob mockActivatedJob;

    final AggregateTestResultHandler sutHandler = new AggregateTestResultHandler();

    @Test
    public void shouldAggregateResultOfDifferentTestsWhichStoreTheirResultInDifferentVariables()
        throws Exception {
      // given
      mockJobCompletChain();

      when(mockActivatedJob.getCustomHeaders())
          .thenReturn(
              Map.of(AggregateTestResultHandler.KEY_VARAIBLENAMES, "testA_result, testB_result"));
      when(mockActivatedJob.getVariablesAsMap())
          .thenReturn(Map.of("testA_result", "PASSED", "testB_result", "FAILED"));

      // when
      sutHandler.handle(mockJobClient, mockActivatedJob);

      verify(mockCompleteJobCommandStep1)
          .variables(Map.of(AggregateTestResultHandler.KEY_AGGREGATED_RESULT, "FAILED"));

      verify(mockJobClient).newCompleteCommand(Mockito.anyLong());
      verify(mockCompleteJobCommandStep2).send();
      verify(mockZeebeFuture).join();

      verifyNoMoreInteractions(mockJobClient);
      verifyNoMoreInteractions(mockCompleteJobCommandStep1);
      verifyNoMoreInteractions(mockCompleteJobCommandStep2);
      verifyNoMoreInteractions(mockZeebeFuture);
    }

    @Test
    public void shouldAggregateResultsOfTestsThatRunAsMultiInstanceAndStoreTheirResultInAnArray()
        throws Exception {
      // given
      mockJobCompletChain();

      when(mockActivatedJob.getCustomHeaders())
          .thenReturn(Map.of(AggregateTestResultHandler.KEY_VARAIBLENAMES, "testResults"));
      when(mockActivatedJob.getVariablesAsMap())
          .thenReturn(Map.of("testResults", List.of("PASSED", "SKIPPED", "FAILED", "PASSED")));

      // when
      sutHandler.handle(mockJobClient, mockActivatedJob);

      verify(mockCompleteJobCommandStep1)
          .variables(Map.of(AggregateTestResultHandler.KEY_AGGREGATED_RESULT, "FAILED"));

      verify(mockJobClient).newCompleteCommand(Mockito.anyLong());
      verify(mockCompleteJobCommandStep2).send();
      verify(mockZeebeFuture).join();

      verifyNoMoreInteractions(mockJobClient);
      verifyNoMoreInteractions(mockCompleteJobCommandStep1);
      verifyNoMoreInteractions(mockCompleteJobCommandStep2);
      verifyNoMoreInteractions(mockZeebeFuture);
    }

    @Test
    public void shoulReturnSkippedWhenAggregatingAnEmpotyList() throws Exception {
      // given
      mockJobCompletChain();

      when(mockActivatedJob.getCustomHeaders())
          .thenReturn(Map.of(AggregateTestResultHandler.KEY_VARAIBLENAMES, "testResults"));
      when(mockActivatedJob.getVariablesAsMap())
          .thenReturn(Map.of("testResults", Collections.EMPTY_LIST));

      // when
      sutHandler.handle(mockJobClient, mockActivatedJob);

      verify(mockCompleteJobCommandStep1)
          .variables(Map.of(AggregateTestResultHandler.KEY_AGGREGATED_RESULT, "SKIPPED"));

      verify(mockJobClient).newCompleteCommand(Mockito.anyLong());
      verify(mockCompleteJobCommandStep2).send();
      verify(mockZeebeFuture).join();

      verifyNoMoreInteractions(mockJobClient);
      verifyNoMoreInteractions(mockCompleteJobCommandStep1);
      verifyNoMoreInteractions(mockCompleteJobCommandStep2);
      verifyNoMoreInteractions(mockZeebeFuture);
    }

    @SuppressWarnings("unchecked")
    private void mockJobCompletChain() {
      when(mockJobClient.newCompleteCommand(Mockito.anyLong()))
          .thenReturn(mockCompleteJobCommandStep1);
      when(mockCompleteJobCommandStep1.variables(Mockito.anyMap()))
          .thenReturn(mockCompleteJobCommandStep2);
      when(mockCompleteJobCommandStep2.send()).thenReturn(mockZeebeFuture);
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
      assertThatThrownBy(() -> sutHandler.addToAggregate(TestResult.SKIPPED, valueToAdd))
          .isInstanceOf(Exception.class);
    }

    @Test
    void shouldRejectNull() throws JsonProcessingException {
      // when + then
      assertThatThrownBy(() -> sutHandler.addToAggregate(TestResult.SKIPPED, null))
          .isInstanceOf(Exception.class);
    }
  }
}
