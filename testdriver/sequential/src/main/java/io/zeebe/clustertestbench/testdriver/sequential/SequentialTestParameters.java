package io.zeebe.clustertestbench.testdriver.sequential;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.camunda.zeebe.client.impl.ZeebeObjectMapper;
import io.zeebe.clustertestbench.testdriver.api.serde.DurationDeserializer;
import io.zeebe.clustertestbench.testdriver.api.serde.DurationSerializer;
import java.time.Duration;

public class SequentialTestParameters {

  private int steps;

  private int iterations;

  private Duration maxTimeForIteration;

  private Duration maxTimeForCompleteTest;

  public int getSteps() {
    return steps;
  }

  public void setSteps(final int steps) {
    this.steps = steps;
  }

  public int getIterations() {
    return iterations;
  }

  public void setIterations(final int iterations) {
    this.iterations = iterations;
  }

  @JsonSerialize(using = DurationSerializer.class)
  public Duration getMaxTimeForIteration() {
    return maxTimeForIteration;
  }

  @JsonDeserialize(using = DurationDeserializer.class)
  public void setMaxTimeForIteration(final Duration maxTimeForIteration) {
    this.maxTimeForIteration = maxTimeForIteration;
  }

  @JsonSerialize(using = DurationSerializer.class)
  public Duration getMaxTimeForCompleteTest() {
    return maxTimeForCompleteTest;
  }

  @JsonDeserialize(using = DurationDeserializer.class)
  public void setMaxTimeForCompleteTest(final Duration maxTimeForCompleteTest) {
    this.maxTimeForCompleteTest = maxTimeForCompleteTest;
  }

  @Override
  public String toString() {
    return new ZeebeObjectMapper().toJson(this);
  }

  public static SequentialTestParameters defaultParams() {
    final SequentialTestParameters result = new SequentialTestParameters();

    result.setSteps(3);

    result.setIterations(10);

    result.setMaxTimeForIteration(Duration.ofSeconds(20));

    result.setMaxTimeForCompleteTest(Duration.ofSeconds(240));

    return result;
  }
}
