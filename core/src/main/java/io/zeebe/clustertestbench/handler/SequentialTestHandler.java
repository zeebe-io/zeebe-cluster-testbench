package io.zeebe.clustertestbench.handler;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.testdriver.api.CamundaCloudAuthenticationDetails;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.api.TestReport;
import io.zeebe.clustertestbench.testdriver.api.TestReport.TestResult;
import io.zeebe.clustertestbench.testdriver.impl.CamundaCLoudAuthenticationDetailsImpl;
import io.zeebe.clustertestbench.testdriver.sequential.SequentialTestDriver;
import io.zeebe.clustertestbench.testdriver.sequential.SequentialTestParameters;

public class SequentialTestHandler implements JobHandler {

	@Override
	public void handle(final JobClient client, final ActivatedJob job) throws Exception {
		final Input input = job.getVariablesAsType(Input.class);

		Thread testDiverThread = new Thread(() -> {
			SequentialTestDriver sequentialTestDriver = new SequentialTestDriver(input.getAuthenticationDetails(),
					input.getTestParameters());

			TestReport testReport = sequentialTestDriver.runTest();

			client.newCompleteCommand(job.getKey()).variables(new Output(testReport)).send();
		});

		testDiverThread.start();
	}

	private static final class Input {
		private CamundaCLoudAuthenticationDetailsImpl authenticationDetails;
		private SequentialTestParameters testParameters;

		@JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
		public CamundaCLoudAuthenticationDetailsImpl getAuthenticationDetails() {
			return authenticationDetails;
		}

		@JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
		public void setAuthenticationDetails(CamundaCLoudAuthenticationDetailsImpl authenticationDetails) {
			this.authenticationDetails = authenticationDetails;
		}

		@JsonProperty(TestDriver.VARIABLE_KEY_TEST_PARAMETERS)
		public SequentialTestParameters getTestParameters() {
			return testParameters;
		}

		@JsonProperty(TestDriver.VARIABLE_KEY_TEST_PARAMETERS)
		public void setTestParameters(SequentialTestParameters testParameters) {
			this.testParameters = testParameters;
		}
	}

	private static final class Output {

		private final TestReport testReport;

		public Output(TestReport testReport) {
			super();
			this.testReport = testReport;
		}

		@JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
		public TestReport getTestReport() {
			return testReport;
		}

		@JsonProperty(TestDriver.VARIABLE_KEY_TEST_RESULT)
		public TestResult getTestResult() {
			return testReport.getTestResult();
		}

	}

}
