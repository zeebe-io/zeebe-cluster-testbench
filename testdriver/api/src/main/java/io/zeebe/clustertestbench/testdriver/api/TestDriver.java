package io.zeebe.clustertestbench.testdriver.api;

public interface TestDriver {

  String VARIABLE_KEY_TEST_PARAMETERS = "testParams";
  String VARIABLE_KEY_TEST_REPORT = "testReport";

  TestReport runTest();
}
