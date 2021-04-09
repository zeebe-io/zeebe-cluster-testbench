package io.zeebe.clustertestbench.testdriver.sequential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.zeebe.clustertestbench.testdriver.sequential.ExceptionFilterBuilder.ProcessNotFoundPredicate;
import java.util.function.Predicate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ExceptionFilterBuilderTest {
  private static final String TEST_PROCESS_ID = "test-process";

  private static Exception createNestedStatusRuntimeException(final Code code) {
    return createNestedStatusRuntimeException(code, "dummy description");
  }

  private static Exception createNestedStatusRuntimeException(
      final Code code, final String description) {
    final StatusRuntimeException ressourceExhaustedException =
        new StatusRuntimeException(Status.fromCode(code).withDescription(description));

    final Exception t = new Exception(ressourceExhaustedException);
    return t;
  }

  @Nested
  @DisplayName("predicate ressource exhausted")
  class ResourceExhaustedPredicateTest {

    private final Predicate<Exception> sutPredicate =
        ExceptionFilterBuilder.RESSOURCE_EXHAUSTED_ERROR_PREDICATE;

    @Test
    void shouldNotMatchForArbitraryExceptions() {
      // given
      final Exception t = new Exception();

      // when
      final boolean match = sutPredicate.test(t);

      // then
      assertThat(match).isFalse();
    }

    @ParameterizedTest
    @EnumSource(mode = EXCLUDE, names = "RESOURCE_EXHAUSTED")
    void shouldNotMatchForStatusRuntimeExceptionsThatAreNotRessourceExhasueted(final Code code) {
      // given
      final Exception t = createNestedStatusRuntimeException(code);

      // when
      final boolean match = sutPredicate.test(t);

      // then
      assertThat(match).isFalse();
    }

    @Test
    void shouldMatchForNextedRessourceExhaustedException() {
      // given
      final Exception t = createNestedStatusRuntimeException(Code.RESOURCE_EXHAUSTED);

      // when
      final boolean match = sutPredicate.test(t);

      // then
      assertThat(match).isTrue();
    }
  }

  @Nested
  @DisplayName("predicate process not found")
  class ProcessNotFoundPredicateTest {

    private final Predicate<Exception> sutPredicate = new ProcessNotFoundPredicate(TEST_PROCESS_ID);

    @Test
    void shouldNotMatchForArbitraryExceptions() {
      // given
      final Exception t = new Exception();

      // when
      final boolean match = sutPredicate.test(t);

      // then
      assertThat(match).isFalse();
    }

    @Test
    void shouldMatchForDifferentProcessMentionedInException() {
      // given
      final Exception t =
          createNestedStatusRuntimeException(
              Code.NOT_FOUND,
              "Command rejected with code 'CREATE_WITH_AWAITING_RESULT': Expected to find process definition with process ID 'some-other-process', but none found");

      // when
      final boolean match = sutPredicate.test(t);

      // then
      assertThat(match).isFalse();
    }

    @Test
    void shouldMatchForExampleException() {
      // given

      // message as was observed in production
      final Exception t =
          createNestedStatusRuntimeException(
              Code.NOT_FOUND,
              "Command rejected with code 'CREATE_WITH_AWAITING_RESULT': Expected to find process definition with process ID 'test-process', but none found");

      // when
      final boolean match = sutPredicate.test(t);

      // then
      assertThat(match).isTrue();
    }

    @Test
    void shouldMatchForArbitrayCommands() {
      // given
      final Exception t =
          createNestedStatusRuntimeException(
              Code.NOT_FOUND,
              "Command rejected with code 'SOME_OTHER_COMMAND': Expected to find process definition with process ID 'test-process', but none found");

      // when
      final boolean match = sutPredicate.test(t);

      // then
      assertThat(match).isTrue();
    }
  }

  @Nested
  @DisplayName("When default exception filter is used")
  class DefaultExceptionFilter {

    @Test
    void shouldTestPositiveForArbitraryException() {
      // given
      final Exception t = new Exception();

      final Predicate<Exception> sutExceptionFilter = new ExceptionFilterBuilder().build();

      // when
      final boolean actual = sutExceptionFilter.test(t);

      // then
      Assertions.assertThat(actual).isTrue();
    }

    @Test
    void shouldTestPositiveForRessourceExhaustedException() {
      // given
      final Exception t = createNestedStatusRuntimeException(Code.RESOURCE_EXHAUSTED);

      final Predicate<Exception> sutExceptionFilter = new ExceptionFilterBuilder().build();

      // when
      final boolean actual = sutExceptionFilter.test(t);

      // then
      Assertions.assertThat(actual).isTrue();
    }
  }

  @Nested
  @DisplayName("When exception filter ignores resource exhausted exceptions")
  class ResourceExhaustedIgnoringErrorFilter {

    @Test
    void shouldTestPositiveForArbitraryException() {
      // given
      final Exception t = new Exception();

      final Predicate<Exception> sutExceptionFilter =
          new ExceptionFilterBuilder().ignoreRessourceExhaustedExceptions().build();

      // when
      final boolean actual = sutExceptionFilter.test(t);

      // then
      Assertions.assertThat(actual).isTrue();
    }

    @Test
    void shouldTestNegativeForRessourceExhaustedException() {
      // given
      final Exception t = createNestedStatusRuntimeException(Code.RESOURCE_EXHAUSTED);

      final Predicate<Exception> sutExceptionFilter =
          new ExceptionFilterBuilder().ignoreRessourceExhaustedExceptions().build();

      // when
      final boolean actual = sutExceptionFilter.test(t);

      // then
      Assertions.assertThat(actual).isFalse();
    }
  }

  @Nested
  @DisplayName("When exception filter ignores process not found exceptions")
  class ProcessNotFoundIgnoringErrorFilter {

    @Test
    void shouldTestPositiveForArbitraryException() {
      // given
      final Exception t = new Exception();

      final Predicate<Exception> sutExceptionFilter =
          new ExceptionFilterBuilder()
              .ignoreProcessNotFoundExceptions(TEST_PROCESS_ID)
              .ignoreRessourceExhaustedExceptions()
              .build();

      // when
      final boolean actual = sutExceptionFilter.test(t);

      // then
      Assertions.assertThat(actual).isTrue();
    }

    @Test
    void shouldTestNegativeForProcessNotFoundException() {
      // given
      final Exception t =
          createNestedStatusRuntimeException(
              Code.NOT_FOUND,
              "Command rejected with code 'CREATE_WITH_AWAITING_RESULT': Expected to find process definition with process ID 'test-process', but none found");

      final Predicate<Exception> sutExceptionFilter =
          new ExceptionFilterBuilder().ignoreProcessNotFoundExceptions(TEST_PROCESS_ID).build();

      // when
      final boolean actual = sutExceptionFilter.test(t);

      // then
      Assertions.assertThat(actual).isFalse();
    }
  }

  @Nested
  @DisplayName(
      "When exception filter ignores process not found exceptions and ressource exhausted exceptions")
  class ErrorFilterWithCombinedPredicated {

    @Test
    void shouldTestPositiveForArbitraryException() {
      // given
      final Exception t = new Exception();

      final Predicate<Exception> sutExceptionFilter =
          new ExceptionFilterBuilder()
              .ignoreRessourceExhaustedExceptions()
              .ignoreProcessNotFoundExceptions(TEST_PROCESS_ID)
              .ignoreRessourceExhaustedExceptions()
              .build();

      // when
      final boolean actual = sutExceptionFilter.test(t);

      // then
      Assertions.assertThat(actual).isTrue();
    }

    @Test
    void shouldTestNegativeForProcessNotFoundException() {
      // given
      final Exception t =
          createNestedStatusRuntimeException(
              Code.NOT_FOUND,
              "Command rejected with code 'CREATE_WITH_AWAITING_RESULT': Expected to find process definition with process ID 'test-process', but none found");

      final Predicate<Exception> sutExceptionFilter =
          new ExceptionFilterBuilder()
              .ignoreProcessNotFoundExceptions(TEST_PROCESS_ID)
              .ignoreRessourceExhaustedExceptions()
              .build();

      // when
      final boolean actual = sutExceptionFilter.test(t);

      // then
      Assertions.assertThat(actual).isFalse();
    }

    @Test
    void shouldTestNegativeForResourceExhaustedException() {
      // given
      final Exception t = createNestedStatusRuntimeException(Code.RESOURCE_EXHAUSTED);

      final Predicate<Exception> sutExceptionFilter =
          new ExceptionFilterBuilder()
              .ignoreProcessNotFoundExceptions(TEST_PROCESS_ID)
              .ignoreRessourceExhaustedExceptions()
              .build();

      // when
      final boolean actual = sutExceptionFilter.test(t);

      // then
      Assertions.assertThat(actual).isFalse();
    }
  }
}
