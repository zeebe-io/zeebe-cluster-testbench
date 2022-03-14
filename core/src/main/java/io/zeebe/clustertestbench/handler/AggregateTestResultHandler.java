package io.zeebe.clustertestbench.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.testdriver.api.TestReport.TestResult;
import io.zeebe.clustertestbench.util.StringLookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * This job handler aggregates test results of different tests.<br>
 * It reads the header field {@code variableNames} to figure out which variables to aggregate. This
 * must be a string of variable names, separated by comma.<br>
 * The variables can contain either a String or a list of String.<br>
 * The aggregator is used in two scenarios:
 *
 * <ul>
 *   <li>aggregate the result of one or more tests (e.g. sequential test and chaos experiments). In
 *       this case the header points to different variables, each storing the test result of one
 *       test
 *   <li>aggregate the result of a multi instance batch of tests (e.g. tests run in different
 *       cluster plans). In this case the header points to a variable that holds an array of strings
 *       for the different items in the multi instance call
 * </ul>
 */
public class AggregateTestResultHandler implements JobHandler {

  protected static final String KEY_VARAIBLENAMES = "variableNames";
  protected static final String KEY_AGGREGATED_RESULT = "aggregatedTestResult";

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    final var headers = job.getCustomHeaders();

    if (!headers.containsKey(KEY_VARAIBLENAMES)) {
      throw new IllegalArgumentException(
          "Variables to aggregate are not defined. Use '"
              + KEY_VARAIBLENAMES
              + "' in a header field to define which variables shall be aggreagted");
    }

    final var commaSeparatedVariableNames = headers.get(KEY_VARAIBLENAMES);

    if (StringUtils.isEmpty(commaSeparatedVariableNames)) {
      throw new IllegalArgumentException(
          "Header field '"
              + KEY_VARAIBLENAMES
              + "' is empty, but expects a comma separeted list of variable names");
    }

    final var variableNameIterator =
        Arrays.stream(commaSeparatedVariableNames.split(",")).map(String::trim).iterator();

    final var variables = new ArrayList<>(job.getVariablesAsMap().entrySet());

    var aggregatedTestResult = TestResult.SKIPPED;

    while (variableNameIterator.hasNext()) {
      final var variableName = variableNameIterator.next();

      final var lookup =
          new StringLookup<>("variable", variableName, variables, Map.Entry::getKey, false);

      final var testResultRaw =
          lookup.lookup().getOrElseThrow(msg -> new RuntimeException(msg)).getValue();

      aggregatedTestResult = addToAggregate(aggregatedTestResult, testResultRaw);
    }

    client
        .newCompleteCommand(job.getKey())
        .variables(Collections.singletonMap(KEY_AGGREGATED_RESULT, aggregatedTestResult.name()))
        .send()
        .join();
  }

  @SuppressWarnings("rawtypes")
  protected TestResult addToAggregate(final TestResult currentAggregate, final Object valueToAdd)
      throws JsonProcessingException {
    if (valueToAdd instanceof String) {
      final var testResult = TestResult.valueOf((String) valueToAdd);

      return TestResult.aggregate(currentAggregate, testResult);
    } else if (valueToAdd instanceof Iterable) {
      final var iterator = ((Iterable) valueToAdd).iterator();

      var combinedAggregate = currentAggregate;
      while (iterator.hasNext()) {
        combinedAggregate = addToAggregate(combinedAggregate, iterator.next());
      }

      return combinedAggregate;
    } else {
      throw new RuntimeException(
          "Unable to aggregate: '"
              + new ObjectMapper().writeValueAsString(valueToAdd)
              + "' The variable content has an unexpected type.");
    }
  }
}
