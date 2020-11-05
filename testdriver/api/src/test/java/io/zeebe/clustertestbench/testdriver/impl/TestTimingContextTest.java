package io.zeebe.clustertestbench.testdriver.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;



@ExtendWith(MockitoExtension.class)
class TestTimingContextTest {
	
	
	@SuppressWarnings({ "resource", "unchecked" })
	@Test
	@DisplayName("constructor should take start time")
	void testConstructorTakesStartTime() {
		
		// given
		Supplier<Long> mockClock = mock(Supplier.class);
		when(mockClock.get()).thenReturn(1l);
		
		// when
		new TestTimingContext(mockClock, Duration.ofSeconds(1), "Test error message", null);
		
		// then
		verify(mockClock).get();
		verifyNoMoreInteractions(mockClock);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("close() should take end time")
	void testCloseTakesEndTime() {
		
		// given
		Supplier<Long> mockClock = mock(Supplier.class);
		when(mockClock.get()).thenReturn(1l);
		
		var sutTimingContext = new TestTimingContext(mockClock, Duration.ofSeconds(1), "Test error message", null);		
		reset(mockClock);
		
		when(mockClock.get()).thenReturn(42l);
		
		// when
		sutTimingContext.close();
		
		// then
		verify(mockClock).get();
		verifyNoMoreInteractions(mockClock);
	}
	
	@SuppressWarnings("resource")
	@Test
	@DisplayName("getStartTime() should return time given by time supplier during construction") 
	void testGetStartTime() {
		// given
		
		TestTimingContext sutTimingContext = new TestTimingContext(()-> 42l, Duration.ofSeconds(1), "Test error message", null);		
		// when
		long actual = sutTimingContext.getStartTime();
		
		// then
		assertThat(actual).isEqualTo(42);
	}

	@SuppressWarnings("resource")
	@Test
	@DisplayName("composeErrorNessage() should only contain error message if no metadata is present")
	void testComposeErrorMessageNoMetaData() {
		// given
		TestTimingContext sutTimingContext = new TestTimingContext(Duration.ofSeconds(1), "Test error message", null);

		// when
		String actual = sutTimingContext.composeErrorMessage();

		// then
		assertThat(actual).startsWith("Test error message").doesNotContain("metaData");
	}
	
	@SuppressWarnings("resource")
	@Test
	@DisplayName("composeErrorNessage() should contain elpasedTime")
	void testComposeErrorMessageShouldShowElapsedTime() {
		// given
		TestTimingContext sutTimingContext = new TestTimingContext(Duration.ofSeconds(1), "Test error message", null);

		// when
		String actual = sutTimingContext.composeErrorMessage();

		// then
		assertThat(actual).contains("elapsedTime: PT0S");
	}

	@SuppressWarnings("resource")
	@Test
	@DisplayName("composeErrorMessage() should contain metadata if present")
	void testComposeErrorMessageWithMetaData() {
		// given
		TestTimingContext sutTimingContext = new TestTimingContext(Duration.ofSeconds(1), "Test error message", null);
		sutTimingContext.putMetaData("key", "value");

		// when
		String actual = sutTimingContext.composeErrorMessage();

		// then
		assertThat(actual).startsWith("Test error message").contains("metaData", "key", "value");
	}
	

	@SuppressWarnings({ "resource", "unchecked" })
	@Test
	@DisplayName("composeErrorMessage() should contain the error message, the elapsed time and the metadata")
	void testComposeErrorMessageAllParts() {
		// given
		Consumer<String> mockErrorCollector = mock(Consumer.class);
		
		Supplier<Long> mockClock = mock(Supplier.class);
		when(mockClock.get()).thenReturn(1000l);
		
		TestTimingContext sutTimingContext = new TestTimingContext(mockClock, Duration.ofSeconds(1), "Test error message", mockErrorCollector);
		sutTimingContext.putMetaData("key", "value");
		
		// when
		when(mockClock.get()).thenReturn(3000l);
		sutTimingContext.close();
		
		String actual = sutTimingContext.composeErrorMessage();
		
		// then		
		assertThat(actual).isEqualTo("Test error message; elapsedTime: PT2S; metaData:{key=value}");
	}
	

}
