package io.zeebe.clustertestbench.testdriver.sequential;

import java.time.Duration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.zeebe.client.impl.ZeebeObjectMapper;
import io.zeebe.clustertestbench.testdriver.api.serde.DurationDeserializer;
import io.zeebe.clustertestbench.testdriver.api.serde.DurationSerializer;

public class SequentialTestParameters {

	private int steps;

	private int iterations;

	private Duration maxTimeForIteration;

	private Duration maxTimeForCompleteTest;

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	@JsonSerialize(using = DurationSerializer.class)
	public Duration getMaxTimeForIteration() {
		return maxTimeForIteration;
	}

	@JsonDeserialize(using = DurationDeserializer.class)
	public void setMaxTimeForIteration(Duration maxTimeForIteration) {
		this.maxTimeForIteration = maxTimeForIteration;
	}

	@JsonSerialize(using = DurationSerializer.class)
	public Duration getMaxTimeForCompleteTest() {
		return maxTimeForCompleteTest;
	}

	@JsonDeserialize(using = DurationDeserializer.class)
	public void setMaxTimeForCompleteTest(Duration maxTimeForCompleteTest) {
		this.maxTimeForCompleteTest = maxTimeForCompleteTest;
	}

	@Override
	public String toString() {
		return new ZeebeObjectMapper().toJson(this);
	}

	public static SequentialTestParameters defaultParams() {
		SequentialTestParameters result = new SequentialTestParameters();

		result.setSteps(3);

		result.setIterations(10);

		result.setMaxTimeForIteration(Duration.ofSeconds(10));

		result.setMaxTimeForCompleteTest(Duration.ofSeconds(120));

		return result;
	}
}
