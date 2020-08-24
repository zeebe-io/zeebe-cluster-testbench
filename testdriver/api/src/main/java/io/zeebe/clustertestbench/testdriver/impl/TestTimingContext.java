package io.zeebe.clustertestbench.testdriver.impl;

import java.time.Duration;
import java.util.function.Consumer;

public class TestTimingContext implements AutoCloseable {

	private final long startTime;

	private final long maxTime;

	private final String errorMessage;

	private final Consumer<String> errorCollector;

	public TestTimingContext(Duration maxTime, String errorMessage, Consumer<String> errorCollector) {
		super();
		this.startTime = System.currentTimeMillis();
		this.maxTime = maxTime.toMillis();
		this.errorMessage = errorMessage;
		this.errorCollector = errorCollector;
	}

	@Override
	public void close() {
		long endTime = System.currentTimeMillis();

		if (endTime - startTime > maxTime) {
			errorCollector.accept(errorMessage);
		}
	}

}
