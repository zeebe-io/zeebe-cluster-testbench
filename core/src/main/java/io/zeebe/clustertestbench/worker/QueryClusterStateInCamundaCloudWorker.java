package io.zeebe.clustertestbench.worker;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClientFactory;
import io.zeebe.clustertestbench.cloud.response.ClusterInfo;
import io.zeebe.clustertestbench.cloud.response.ClusterStatus;

public class QueryClusterStateInCamundaCloudWorker implements JobHandler {

	private static final Logger logger = LoggerFactory.getLogger(QueryClusterStateInCamundaCloudWorker.class);

	private final CloudAPIClient cloudClient;

	public QueryClusterStateInCamundaCloudWorker(String cloudApiUrl, String cloudApiAuthenticationServerURL,
			String cloudApiAudience, String cloudApiClientId, String cloudApiClientSecret) {
		this.cloudClient = new CloudAPIClientFactory().createCloudAPIClient(cloudApiUrl,
				cloudApiAuthenticationServerURL, cloudApiAudience, cloudApiClientId, cloudApiClientSecret);
	}

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		final Input input = job.getVariablesAsType(Input.class);

		String clusterStatus = Optional.ofNullable(cloudClient.getClusterInfo(input.getClusterId()))
				.map(ClusterInfo::getStatus).map(ClusterStatus::getReady).orElse("Unknown");

		logger.info("Status of cluster " + input.getClusterName() + " " + clusterStatus);

		client.newCompleteCommand(job.getKey()).variables(new Output(clusterStatus)).send();

	}

	private static final class Input {
		private String clusterId;
		private String clusterName;

		public String getClusterId() {
			return clusterId;
		}

		public void setClusterId(String clusterId) {
			this.clusterId = clusterId;
		}

		public String getClusterName() {
			return clusterName;
		}

		public void setClusterName(String clusterName) {
			this.clusterName = clusterName;
		}

	}

	private static final class Output {

		private final String clusterStatus;

		public Output(String clusterStatus) {
			this.clusterStatus = clusterStatus;
		}

		public String getClusterStatus() {
			return clusterStatus;
		}
	}
}
