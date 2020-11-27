package io.zeebe.clustertestbench.testdriver.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TestTimingContext implements AutoCloseable {

  private final Supplier<Long> timeSupplier;

  private final long startTime;
  private long elapsedTime;
  private long endTime;

  private final long maxDuration;

  private final String errorMessage;

  private final Consumer<String> errorCollector;

  private final Map<String, Object> metaData = new HashMap<>();

  public TestTimingContext(
      final Duration maxTime, final String errorMessage, final Consumer<String> errorCollector) {
    this(System::currentTimeMillis, maxTime, errorMessage, errorCollector);
  }

  protected TestTimingContext(
      final Supplier<Long> timeSupplier,
      final Duration maxDuration,
      final String errorMessage,
      final Consumer<String> errorCollector) {
    super();
    this.timeSupplier = timeSupplier;
    this.startTime = timeSupplier.get();
    this.maxDuration = maxDuration.toMillis();
    this.errorMessage = errorMessage;
    this.errorCollector = errorCollector;
  }

  public long getStartTime() {
    return startTime;
  }

  protected String composeErrorMessage() {
    final StringBuilder errorMessageBuilder = new StringBuilder();

    errorMessageBuilder.append(errorMessage);

    errorMessageBuilder.append("; elapsedTime: ").append(Duration.ofMillis(elapsedTime));

    if (!metaData.isEmpty()) {
      errorMessageBuilder.append("; metaData:").append(metaData);
    }

    return errorMessageBuilder.toString();
  }

  public void putMetaData(final String key, final Object value) {
    metaData.put(key, value);
  }

  @Override
  public void close() {
    endTime = timeSupplier.get();

    elapsedTime = endTime - startTime;

    if (elapsedTime > maxDuration) {
      errorCollector.accept(composeErrorMessage());
    }
  }
}
