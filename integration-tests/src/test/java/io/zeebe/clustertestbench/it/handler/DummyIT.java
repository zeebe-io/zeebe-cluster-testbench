package io.zeebe.clustertestbench.it.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** This IT exists to record a test report, even when all other integration tests are disabled. */
public class DummyIT {

  @Test
  void testSomethingUseless() {
    assertThat(true).isTrue();
  }
}
