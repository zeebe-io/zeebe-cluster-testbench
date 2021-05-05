package io.zeebe.clustertestbench.testdriver.sequential;

import io.camunda.zeebe.client.impl.ZeebeObjectMapper;
import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SequentialTestParametersTest {

  @Test
  void testSerializeAndDeserialize() {
    // given
    final SequentialTestParameters original = new SequentialTestParameters();
    original.setIterations(42);
    original.setSteps(3);
    original.setMaxTimeForCompleteTest(Duration.ofMinutes(2));
    original.setMaxTimeForIteration(Duration.ofSeconds(28));

    // when
    final String jsonString = new ZeebeObjectMapper().toJson(original);

    final SequentialTestParameters actual =
        new ZeebeObjectMapper().fromJson(jsonString, SequentialTestParameters.class);

    Assertions.assertThat(actual).isEqualToComparingFieldByField(original);
  }
}
