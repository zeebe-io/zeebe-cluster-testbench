package io.zeebe.template;

public class App {

	public String foo() { 
		return "Hello World";
	}

	public static void main(String[] args) {
		System.out.println(new App().foo());
	}

}
