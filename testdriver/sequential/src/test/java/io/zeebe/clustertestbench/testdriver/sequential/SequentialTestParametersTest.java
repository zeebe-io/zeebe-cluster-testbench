package io.zeebe.clustertestbench.testdriver.sequential;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.zeebe.client.impl.ZeebeObjectMapper;

class SequentialTestParametersTest {

	@Test
	void testSerializeAndDeserialize() {
		// given
		SequentialTestParameters original = new SequentialTestParameters();
		original.setIterations(42);
		original.setSteps(3);
		original.setMaxTimeForCompleteTest(Duration.ofMinutes(2));
		original.setMaxTimeForIteration(Duration.ofSeconds(28));
		
		// when
		String jsonString = new ZeebeObjectMapper().toJson(original);
		
		SequentialTestParameters actual = new ZeebeObjectMapper().fromJson(jsonString, SequentialTestParameters.class);
		
		Assertions.assertThat(actual).isEqualToComparingFieldByField(original);
	}

}
