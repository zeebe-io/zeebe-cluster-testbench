package io.zeebe.clustertestbench.worker;

import static org.awaitility.Awaitility.with;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.awaitility.core.ConditionTimeoutException;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClientFactory;
import io.zeebe.clustertestbench.cloud.request.CreateClusterRequest;
import io.zeebe.clustertestbench.cloud.request.CreateZeebeClientRequest;
import io.zeebe.clustertestbench.cloud.response.ClusterInfo;
import io.zeebe.clustertestbench.cloud.response.ClusterStatus;
import io.zeebe.clustertestbench.cloud.response.CreateClusterResponse;
import io.zeebe.clustertestbench.cloud.response.CreateZeebeClientResponse;
import io.zeebe.clustertestbench.cloud.response.ZeebeClientConnectiontInfo;
import io.zeebe.clustertestbench.testdriver.api.CamundaCloudAuthenticationDetails;
import io.zeebe.clustertestbench.testdriver.impl.CamundaCLoudAuthenticationDetailsImpl;

public class CreateClusterInCamundaCloudWorker implements JobHandler {

	private static final RandomNameGenerator NAME_GENRATOR = new RandomNameGenerator();

	private final CloudAPIClient cloudClient;

	public CreateClusterInCamundaCloudWorker(String cloudApiUrl, String cloudApiAuthenticationServerURL,
			String cloudApiAudience, String cloudApiClientId, String cloudApiClientSecret) {
		this.cloudClient = new CloudAPIClientFactory().createCloudAPIClient(cloudApiUrl,
				cloudApiAuthenticationServerURL, cloudApiAudience, cloudApiClientId, cloudApiClientSecret);
	}

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		final Input input = job.getVariablesAsType(Input.class);

		String name = NAME_GENRATOR.next();

		CreateClusterResponse createClusterRepoonse = cloudClient.createCluster(new CreateClusterRequest(name,
				input.getClusterPlan(), input.getChannelId(), input.getDockerImage(), input.getRegionId()));

		String clusterId = createClusterRepoonse.getClusterId();

		CreateZeebeClientResponse createZeebeClientResponse = cloudClient.createZeebeClient(clusterId,
				new CreateZeebeClientRequest(name + "_client"));

		ZeebeClientConnectiontInfo connectionInfo = cloudClient.getZeebeClientInfo(clusterId,
				createZeebeClientResponse.getClientId());

		try {
			with().pollDelay(15, TimeUnit.SECONDS)// wait a couple of seconds for the DNS record to show up; otherwise
													// the missing DNS record may be cached prematurely
					.and().pollInterval(10, TimeUnit.SECONDS).await().atMost(15, TimeUnit.MINUTES)
					.until(() -> clusterIsReady(clusterId));

			client.newCompleteCommand(job.getKey()).variables(new Output(connectionInfo, clusterId)).send();
		} catch (ConditionTimeoutException e) {
			cloudClient.deleteCluster(clusterId);

			client.newFailCommand(job.getKey()).retries(job.getRetries() - 1)
					.errorMessage("Cluster took too long to start");
		}
	}

	private boolean clusterIsReady(String clusterId) {
		String readyStatus = Optional.of(cloudClient.getClusterInfo(clusterId)).map(ClusterInfo::getStatus)
				.map(ClusterStatus::getReady).orElse("Unknown");

		return readyStatus.equalsIgnoreCase("healthy");
	}

	private static final class Input {
		private String dockerImage;
		private String clusterPlan;
		private String regionId;
		private String channelId;

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

		public String getRegionId() {
			return regionId;
		}

		public void setRegionId(String regionId) {
			this.regionId = regionId;
		}

		public String getChannelId() {
			return channelId;
		}

		public void setChannelId(String channelId) {
			this.channelId = channelId;
		}
	}

	private static final class Output {

		private CamundaCLoudAuthenticationDetailsImpl authenticationDetails;
		private String clusterId;

		public Output(ZeebeClientConnectiontInfo connectionInfo, String clusterId) {
			this.authenticationDetails = new CamundaCLoudAuthenticationDetailsImpl(
					connectionInfo.getZeebeAuthorizationServerUrl(), connectionInfo.getZeebeAudience(),
					connectionInfo.getZeebeAddress(), connectionInfo.getZeebeClientId(),
					connectionInfo.getZeebeClientSecret());
			this.clusterId = clusterId;
		}

		@JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
		public CamundaCLoudAuthenticationDetailsImpl getAuthenticationDetails() {
			return authenticationDetails;
		}

		@JsonProperty(CamundaCloudAuthenticationDetails.VARIABLE_KEY)
		public void setAuthenticationDetails(CamundaCLoudAuthenticationDetailsImpl authenticationDetails) {
			this.authenticationDetails = authenticationDetails;
		}

		public String getClusterId() {
			return clusterId;
		}

		public void setClusterId(String clusterId) {
			this.clusterId = clusterId;
		}
	}
}
