package io.zeebe.clustertestbench.handler;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClientFactory;

public class DeleteClusterInCamundaCloudHandler implements JobHandler {

	private final CloudAPIClient cloudClient;

	public DeleteClusterInCamundaCloudHandler(String cloudApiUrl, String cloudApiAuthenticationServerURL, String cloudApiAudience,
			String cloudApiClientId, String cloudApiClientSecret) {
		this.cloudClient = new CloudAPIClientFactory().createCloudAPIClient(cloudApiUrl, cloudApiAuthenticationServerURL,
				cloudApiAudience, cloudApiClientId, cloudApiClientSecret);
	}

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		final Input input = job.getVariablesAsType(Input.class);

		cloudClient.deleteCluster(input.getClusterId());

		client.newCompleteCommand(job.getKey()).send();
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
}
