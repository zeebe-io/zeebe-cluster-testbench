package io.zeebe.clustertestbench.testdriver.api;

import static io.zeebe.clustertestbench.testdriver.api.TestDriver.VARIABLE_KEY_TEST_PARAMETERS;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.zeebe.clustertestbench.testdriver.api.TestDriver.TestResult;
import java.util.List;

/**
 * This interface is here to illustrate the logical interface of {@code
 * run-test-suite-in-camuna-cloud.bpmn}
 */
public interface TestSuite {

  TestResult runTestSuite(final List<TestDTO> tests);

  record TestDTO<TEST_PARAMS>(
      String testProcessId,
      String generation,
      String channel,
      String region,
      List<String> clusterPlanNames,
      @JsonProperty(VARIABLE_KEY_TEST_PARAMETERS) TEST_PARAMS testParams) {}
}
