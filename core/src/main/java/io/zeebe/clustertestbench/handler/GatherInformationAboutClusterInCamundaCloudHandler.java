package io.zeebe.clustertestbench.handler;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClientFactory;

public class GatherInformationAboutClusterInCamundaCloudHandler implements JobHandler {

	private final CloudAPIClient cloudClient;

	public GatherInformationAboutClusterInCamundaCloudHandler(String cloudApiUrl, String cloudApiAuthenticationServerURL,
			String cloudApiAudience, String cloudApiClientId, String cloudApiClientSecret) {
		this.cloudClient = new CloudAPIClientFactory().createCloudAPIClient(cloudApiUrl,
				cloudApiAuthenticationServerURL, cloudApiAudience, cloudApiClientId, cloudApiClientSecret);
	}

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		final Input input = job.getVariablesAsType(Input.class);

		String operateURL = cloudClient.getClusterInfo(input.getClusterId()).getStatus().getOperateUrl();

		client.newCompleteCommand(job.getKey()).variables(new Output(operateURL)).send();
	}

	private static final class Input {
		private String clusterId;

		public String getClusterId() {
			return clusterId;
		}

		public void setClusterId(String clusterId) {
			this.clusterId = clusterId;
		}
	}

	private static final class Output {

		private final String operateURL;

		public Output(String operateURL) {
			this.operateURL = operateURL;
		}

		public String getOperateURL() {
			return operateURL;
		}

	}
}
