package io.zeebe.clustertestbench.worker;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.testdriver.api.CamundaCLoudAuthenticationDetails;
import io.zeebe.clustertestbench.testdriver.sequential.SequentialTestDriver;

public class RunSequentialTestWorker implements JobHandler {

	private static final int ITERATIONS = 1;
	private static final int MAX_TIME_PER_ITERATION = 5000;

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		Input input = job.getVariablesAsType(Input.class);

		SequentialTestDriver sequentialTestDriver = new SequentialTestDriver(input.getAuthenticationDetails());

		Map<String, Object> variables = new HashMap<>();
		variables.put("testResult", "FAILED");
		variables.put("iterations", ITERATIONS);

		try {
			long startTime = System.currentTimeMillis();
			variables.put("startedAt", startTime);

			boolean pass = sequentialTestDriver.runTest(ITERATIONS);

			long endTime = System.currentTimeMillis();
			variables.put("finishedAt", startTime);

			if (pass) {
				if (endTime - startTime < MAX_TIME_PER_ITERATION * ITERATIONS) {
					variables.put("testResult", "PASSED");
				}
			}
		} finally {
			client.newCompleteCommand(job.getKey()).variables(variables).send();
		}
	}

	private static final class Input {

		private CamundaCLoudAuthenticationDetails authenticationDetails;

		@JsonProperty(CamundaCLoudAuthenticationDetails.VARIABLE_KEY)
		public CamundaCLoudAuthenticationDetails getAuthenticationDetails() {
			return authenticationDetails;
		}

		@JsonProperty(CamundaCLoudAuthenticationDetails.VARIABLE_KEY)
		public void setAuthenticationDetails(CamundaCLoudAuthenticationDetails authenticationDetails) {
			this.authenticationDetails = authenticationDetails;
		}

	}

}
