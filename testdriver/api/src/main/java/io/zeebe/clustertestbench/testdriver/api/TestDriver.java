package io.zeebe.clustertestbench.testdriver.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This interface is here to illustrate the logical interface of a test driver. A test driver is
 * responsible for running a certain test in a cluster that has been provided by testbench
 *
 * @param <TEST_PARAMS> additional input parameters for the test
 */
public interface TestDriver<TEST_PARAMS> {

  String VARIABLE_KEY_TEST_PARAMETERS = "testParams";
  String VARIABLE_KEY_TEST_REPORT = "testReport";
  String VARIABLE_KEY_AUTHENTICATION_DETAILS = "authenticationDetails";

  CompletableFuture<TestOutput> runTest(TestInputDTO<TEST_PARAMS> input);

  @JsonIgnoreProperties(ignoreUnknown = true)
  record TestInputDTO<TEST_PARAMS>(
      String testProcessId,
      String generation,
      String generationUUID,
      String clusterPlan,
      String clusterPlanUUID,
      String channel,
      String channelUUID,
      String region,
      String regionUUID,
      @JsonProperty(VARIABLE_KEY_AUTHENTICATION_DETAILS)
          CamundaCloudAuthenticationDetails authenticationDetails,
      @JsonProperty(VARIABLE_KEY_TEST_PARAMETERS) TEST_PARAMS testParams) {}

  record CamundaCloudAuthenticationDetails(
      String audience,
      String authorizationURL,
      String clientId,
      String clientSecret,
      String contactPoint) {

    public OAuthCredentialsProvider buildCredentialsProvider() {
      if (authorizationURL == null) {
        return new OAuthCredentialsProviderBuilder()
            .audience(audience)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
      } else {
        return new OAuthCredentialsProviderBuilder()
            .authorizationServerUrl(authorizationURL)
            .audience(audience)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
      }
    }
  }

  /** Minimal implementation of {@code TestOutput} */
  record TestOutputDTO(TestReport testReport) implements TestOutput {}

  /** Minimal implementation of {@code TestReport} */
  record TestReportDTO(TestResult testResult, int failureCount, List<String> failureMessages)
      implements TestReport {}

  interface TestOutput {
    @JsonProperty(VARIABLE_KEY_TEST_REPORT)
    TestReport testReport();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  interface TestReport {
    @JsonProperty("testResult")
    TestResult testResult();

    @JsonProperty("failureMessages")
    List<String> failureMessages();

    @JsonProperty("failureCount")
    int failureCount();
  }

  enum TestResult {
    PASSED,
    FAILED,
    SKIPPED; // currently used by the chaos experiments

    public static TestResult aggregate(final TestResult input1, final TestResult input2) {
      if ((input1 == FAILED) || (input2 == FAILED)) {
        return FAILED;
      } else if ((input1 == PASSED) || (input2 == PASSED)) {
        return PASSED;
      } else {
        return SKIPPED;
      }
    }
  }
}
