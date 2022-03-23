package io.zeebe.clustertestbench.testdriver.sequential;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestTimingContextTest {

  @SuppressWarnings({"resource", "unchecked"})
  @Test
  @DisplayName("constructor should take start time")
  void testConstructorTakesStartTime() {

    // given
    final Supplier<Long> mockClock = Mockito.mock(Supplier.class);
    Mockito.when(mockClock.get()).thenReturn(1L);

    // when
    new TestTimingContext(mockClock, Duration.ofSeconds(1), "Test error message", null);

    // then
    Mockito.verify(mockClock).get();
    Mockito.verifyNoMoreInteractions(mockClock);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("close() should take end time")
  void testCloseTakesEndTime() {

    // given
    final Supplier<Long> mockClock = Mockito.mock(Supplier.class);
    Mockito.when(mockClock.get()).thenReturn(1L);

    final var sutTimingContext =
        new TestTimingContext(mockClock, Duration.ofSeconds(1), "Test error message", null);
    Mockito.reset(mockClock);

    Mockito.when(mockClock.get()).thenReturn(42L);

    // when
    sutTimingContext.close();

    // then
    Mockito.verify(mockClock).get();
    Mockito.verifyNoMoreInteractions(mockClock);
  }

  @SuppressWarnings("resource")
  @Test
  @DisplayName("getStartTime() should return time given by time supplier during construction")
  void testGetStartTime() {
    // given

    final TestTimingContext sutTimingContext =
        new TestTimingContext(() -> 42L, Duration.ofSeconds(1), "Test error message", null);
    // when
    final long actual = sutTimingContext.getStartTime();

    // then
    assertThat(actual).isEqualTo(42);
  }

  @SuppressWarnings("resource")
  @Test
  @DisplayName("composeErrorNessage() should only contain error message if no metadata is present")
  void testComposeErrorMessageNoMetaData() {
    // given
    final TestTimingContext sutTimingContext =
        new TestTimingContext(Duration.ofSeconds(1), "Test error message", null);

    // when
    final String actual = sutTimingContext.composeErrorMessage();

    // then
    assertThat(actual).startsWith("Test error message").doesNotContain("metaData");
  }

  @SuppressWarnings("resource")
  @Test
  @DisplayName("composeErrorNessage() should contain elpasedTime")
  void testComposeErrorMessageShouldShowElapsedTime() {
    // given
    final TestTimingContext sutTimingContext =
        new TestTimingContext(Duration.ofSeconds(1), "Test error message", null);

    // when
    final String actual = sutTimingContext.composeErrorMessage();

    // then
    assertThat(actual).contains("elapsedTime: PT0S");
  }

  @SuppressWarnings("resource")
  @Test
  @DisplayName("composeErrorMessage() should contain metadata if present")
  void testComposeErrorMessageWithMetaData() {
    // given
    final TestTimingContext sutTimingContext =
        new TestTimingContext(Duration.ofSeconds(1), "Test error message", null);
    sutTimingContext.putMetaData("key", "value");

    // when
    final String actual = sutTimingContext.composeErrorMessage();

    // then
    assertThat(actual).startsWith("Test error message").contains("metaData", "key", "value");
  }

  @SuppressWarnings({"resource", "unchecked"})
  @Test
  @DisplayName(
      "composeErrorMessage() should contain the error message, the elapsed time and the metadata")
  void testComposeErrorMessageAllParts() {
    // given
    final Consumer<String> mockErrorCollector = Mockito.mock(Consumer.class);

    final Supplier<Long> mockClock = Mockito.mock(Supplier.class);
    Mockito.when(mockClock.get()).thenReturn(1000L);

    final TestTimingContext sutTimingContext =
        new TestTimingContext(
            mockClock, Duration.ofSeconds(1), "Test error message", mockErrorCollector);
    sutTimingContext.putMetaData("key", "value");

    // when
    Mockito.when(mockClock.get()).thenReturn(3000L);
    sutTimingContext.close();

    final String actual = sutTimingContext.composeErrorMessage();

    // then
    assertThat(actual).isEqualTo("Test error message; elapsedTime: PT2S; metaData:{key=value}");
  }
}
