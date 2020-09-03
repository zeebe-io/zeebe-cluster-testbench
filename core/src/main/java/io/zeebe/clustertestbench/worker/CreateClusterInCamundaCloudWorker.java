package io.zeebe.clustertestbench.worker;

import static org.awaitility.Awaitility.with;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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

	private static final Logger logger = Logger.getLogger("io.zeebe.clustertestbench.worker");

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

		logger.info("Creating cluster" + name);
		CreateClusterResponse createClusterRepoonse = cloudClient.createCluster(new CreateClusterRequest(name,
				input.getClusterPlanUUID(), input.getChannelUUID(), input.getGenerationUUID(), input.getRegionUUID()));

		String clusterId = createClusterRepoonse.getClusterId();

		CreateZeebeClientResponse createZeebeClientResponse = cloudClient.createZeebeClient(clusterId,
				new CreateZeebeClientRequest(name + "_client"));

		ZeebeClientConnectiontInfo connectionInfo = cloudClient.getZeebeClientInfo(clusterId,
				createZeebeClientResponse.getClientId());

		try {
			with().pollDelay(3, TimeUnit.SECONDS).and().pollInterval(10, TimeUnit.SECONDS).await()
					.atMost(15, TimeUnit.MINUTES).until(() -> clusterIsReady(clusterId, name));
			
			String operateURL = cloudClient.getClusterInfo(clusterId).getStatus().getOperateUrl();

			client.newCompleteCommand(job.getKey()).variables(new Output(connectionInfo, name, clusterId, operateURL)).send();
		} catch (ConditionTimeoutException e) {
			cloudClient.deleteCluster(clusterId);

			client.newFailCommand(job.getKey()).retries(job.getRetries() - 1)
					.errorMessage("Cluster took too long to start");
		}
	}

	private boolean clusterIsReady(String clusterId, String clusterName) {
		String readyStatus = Optional.of(cloudClient.getClusterInfo(clusterId)).map(ClusterInfo::getStatus)
				.map(ClusterStatus::getReady).orElse("Unknown");

		logger.info("Checking status of '" + clusterName + "`: " + readyStatus);

		return readyStatus.equalsIgnoreCase("healthy");
	}

	private static final class Input {
		private String generationUUID;
		private String regionUUID;
		private String clusterPlanUUID;
		private String channelUUID;

		public String getGenerationUUID() {
			return generationUUID;
		}

		public void setGenerationUUID(String generationUUID) {
			this.generationUUID = generationUUID;
		}

		public String getRegionUUID() {
			return regionUUID;
		}

		public void setRegionUUID(String regionUUID) {
			this.regionUUID = regionUUID;
		}

		public String getClusterPlanUUID() {
			return clusterPlanUUID;
		}

		public void setClusterPlanUUID(String clusterPlanUUID) {
			this.clusterPlanUUID = clusterPlanUUID;
		}

		public String getChannelUUID() {
			return channelUUID;
		}

		public void setChannelUUID(String channelUUID) {
			this.channelUUID = channelUUID;
		}

	}

	private static final class Output {

		private CamundaCLoudAuthenticationDetailsImpl authenticationDetails;
		private String clusterId;
		private String clusterName;
		private String operateURL;
		

		public Output(ZeebeClientConnectiontInfo connectionInfo, String clusterName, String clusterId, String operateURL) {
			this.authenticationDetails = new CamundaCLoudAuthenticationDetailsImpl(
					connectionInfo.getZeebeAuthorizationServerUrl(), connectionInfo.getZeebeAudience(),
					connectionInfo.getZeebeAddress(), connectionInfo.getZeebeClientId(),
					connectionInfo.getZeebeClientSecret());
			this.clusterName = clusterName;			
			this.clusterId = clusterId;
			this.operateURL = operateURL;
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

		public String getClusterName() {
			return clusterName;
		}

		public void setClusterName(String clusterName) {
			this.clusterName = clusterName;
		}

		public String getOperateURL() {
			return operateURL;
		}

		public void setOperateURL(String operateURL) {
			this.operateURL = operateURL;
		}

	}
}
