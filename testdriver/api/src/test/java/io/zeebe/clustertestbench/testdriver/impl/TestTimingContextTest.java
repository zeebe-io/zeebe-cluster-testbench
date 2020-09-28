package io.zeebe.clustertestbench.testdriver.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TestTimingContextTest {

	@Test
	@DisplayName("compose error message should only contain error message if no metadata is present")
	void testComposeErrorMessageNoMetaData() {
		// given
		TestTimingContext sut = new TestTimingContext(Duration.ofSeconds(1), "Test error message", null);

		// when
		String actual = sut.composeErrorMessage();

		// then
		assertThat(actual).isEqualTo("Test error message");
	}

	@Test
	@DisplayName("compose error message should contain metadata if present")
	void testComposeErrorMessageWithMetaData() {
		// given
		TestTimingContext sut = new TestTimingContext(Duration.ofSeconds(1), "Test error message", null);
		sut.putMetaData("key", "value");

		String actual = sut.composeErrorMessage();

		assertThat(actual).startsWith("Test error message").contains("key", "value");
	}

}
