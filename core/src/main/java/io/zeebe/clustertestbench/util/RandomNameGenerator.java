package io.zeebe.clustertestbench.util;

import java.util.Random;

public class RandomNameGenerator {

  private static final String[] EVENT =
      new String[] {
        "none",
        "message",
        "timer",
        "conditional",
        "link",
        "signal",
        "error",
        "escalation",
        "termination",
        "compensation",
        "cancel",
        "multiple",
        "multiple_parallel"
      };

  private static final String[] GATEWAY =
      new String[] {"exclusive", "inclusive", "parallel", "event"};

  private static final String[] ACTIVITY =
      new String[] {"task", "subprocess", "call_activity", "event_subprocess", "transaction"};

  private static final Random RNG = new Random();

  public String next() {
    return getRandomEntry(EVENT) + "-" + getRandomEntry(GATEWAY) + "-" + getRandomEntry(ACTIVITY);
  }

  private String getRandomEntry(final String[] options) {
    return options[RNG.nextInt(options.length)];
  }
}
