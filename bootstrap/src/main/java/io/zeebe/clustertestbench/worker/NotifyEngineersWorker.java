package io.zeebe.clustertestbench.worker;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.testdriver.api.TestDriver;
import io.zeebe.clustertestbench.testdriver.impl.TestReportDTO;

public class NotifyEngineersWorker implements JobHandler {

	private static final String CHANNEL_HEADER_KEY = "channel";
	private static final String TEST_TYPE_HEADER_KEY = "testType";
	private static final String DEFAULT_CHANNEL = "#slack-bot-dev";
	private static final String DEFAULT_TEST_TYPE = "(undefined test)";

	private final MethodsClient slackClient;

	public NotifyEngineersWorker(String token) {
		Slack slack = Slack.getInstance();

		slackClient = slack.methods(token);
	}

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		Map<String, String> headers = job.getCustomHeaders();

		String channel = Optional.ofNullable(headers.get(CHANNEL_HEADER_KEY)).orElse(DEFAULT_CHANNEL);
		String testType = Optional.ofNullable(headers.get(TEST_TYPE_HEADER_KEY)).orElse(DEFAULT_TEST_TYPE);

		final Input input = job.getVariablesAsType(Input.class);

		ChatPostMessageRequest request = ChatPostMessageRequest.builder().channel(channel)
				.text(":bpmn-error-throw-event: \n_" + testType + "`_ on _" + input.getClusterPlan()
						+ "_ failed for image `" + input.getDockerImage() + "`.\nThere were "
						+ input.getTestReport().getFailureCount() + " failures.")
				.build();

		ChatPostMessageResponse response = slackClient.chatPostMessage(request);
		if (response.getError() != null) {
			throw new Exception(response.getError());
		} else {
			client.newCompleteCommand(job.getKey()).send();
		}
	}

	private static final class Input {
		private String dockerImage;
		private String clusterPlan;
		private String clusterId;

		private TestReportDTO testReport;

		public String getDockerImage() {
			return dockerImage;
		}

		public void setDockerImage(String dockerImage) {
			this.dockerImage = dockerImage;
		}

		public String getClusterPlan() {
			return clusterPlan;
		}

		public void setClusterPlan(String clusterPlan) {
			this.clusterPlan = clusterPlan;
		}

		public String getClusterId() {
			return clusterId;
		}

		public void setClusterId(String clusterId) {
			this.clusterId = clusterId;
		}

		@JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
		public TestReportDTO getTestReport() {
			return testReport;
		}

		@JsonProperty(TestDriver.VARIABLE_KEY_TEST_REPORT)
		public void setTestReport(TestReportDTO testReport) {
			this.testReport = testReport;
		}

	}
}
