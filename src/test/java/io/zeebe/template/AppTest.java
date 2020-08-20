package io.zeebe.template;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.zeebe.template.App;

public class AppTest {


	@Test
	public void testFoo() {
		String actual = new App().foo();
		
		Assertions.assertThat(actual).isEqualTo("Hello World");
	}
}
