package io.zeebe.clustertestbench.handler;

import java.util.Random;

public class RandomNameGenerator {

	private static final String[] event = new String[] { "none", "message", "timer", "conditional", "link", "signal",
			"error", "escalation", "termination", "compensation", "cancel", "multiple", "multiple_parallel" };

	private static final String[] gateway = new String[] { "exclusive", "inclusive", "parallel", "event" };

	private static final String[] activity = new String[] { "task", "subprocess", "call_activity", "event_subprocess",
			"transaction" };

	private static final Random RNG = new Random();

	public String next() {
		return getRandomEntry(event) + "-" + getRandomEntry(gateway) + "-" + getRandomEntry(activity);
	}

	private String getRandomEntry(String[] options) {
		return options[RNG.nextInt(options.length)];
	}

}
