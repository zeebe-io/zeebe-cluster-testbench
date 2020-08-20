package io.zeebe.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlakyTest {

	private static int counter = 0;

	@Test
	public void demonstracteFlakyTest() {
		System.out.println("System out for first flaky test run");
		if (counter == 0) {
			counter++;
			Assertions.fail("Deliberate flaky test failure");
		}
	}
}
