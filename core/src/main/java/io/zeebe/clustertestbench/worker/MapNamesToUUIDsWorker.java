package io.zeebe.clustertestbench.worker;

import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClientFactory;
import io.zeebe.clustertestbench.cloud.response.ChannelInfo;
import io.zeebe.clustertestbench.cloud.response.ClusterPlanTypeInfo;
import io.zeebe.clustertestbench.cloud.response.GenerationInfo;
import io.zeebe.clustertestbench.cloud.response.ParametersResponse;
import io.zeebe.clustertestbench.cloud.response.RegionInfo;

public class MapNamesToUUIDsWorker implements JobHandler {

	private static final Logger logger = LoggerFactory.getLogger(MapNamesToUUIDsWorker.class);

	private final CloudAPIClient cloudClient;

	public MapNamesToUUIDsWorker(String cloudApiUrl, String cloudApiAuthenticationServerURL, String cloudApiAudience,
			String cloudApiClientId, String cloudApiClientSecret) {
		this.cloudClient = new CloudAPIClientFactory().createCloudAPIClient(cloudApiUrl,
				cloudApiAuthenticationServerURL, cloudApiAudience, cloudApiClientId, cloudApiClientSecret);
	}

	@Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
		logger.info("Mapping names to UUIDs and vice versa");
		final InputOutput inputOutput = job.getVariablesAsType(InputOutput.class);
		
		logger.info("Input: " + inputOutput);

		ParametersResponse parameters = cloudClient.getParameters();

		final ChannelInfo channelInfo = mapChannel(inputOutput, parameters);

		mapGeneration(inputOutput, channelInfo);

		mapClusterPlan(inputOutput, parameters);
		mapRegion(inputOutput, parameters);

		logger.info("Output: " + inputOutput);
		client.newCompleteCommand(job.getKey()).variables(inputOutput).send();
	}

	private ChannelInfo mapChannel(InputOutput inputOutput, ParametersResponse parameters) {
		final ChannelInfo channelInfo;

		if ((inputOutput.getChannelUUID() == null) && (inputOutput.getChannel() == null)) {
			throw new IllegalArgumentException("Neither 'generation' nor 'generationUUID' are provided");
		}

		final String notFoundMessage;
		final Predicate<ChannelInfo> channelFilter;

		if (inputOutput.getChannelUUID() == null) {
			notFoundMessage = "Unable to find channel with name " + inputOutput.getChannel();
			channelFilter = (item) -> inputOutput.getChannel().equalsIgnoreCase(item.getName());
		} else {
			notFoundMessage = "Unable to find channel with UUID " + inputOutput.getChannelUUID();
			channelFilter = (item) -> inputOutput.getChannelUUID().equals(item.getUuid());
		}

		Optional<ChannelInfo> optChannelInfo = parameters.getChannels().stream().filter(channelFilter).findFirst();

		if (optChannelInfo.isEmpty()) {
			throw new IllegalArgumentException(notFoundMessage + ". " + parameters.getChannels().toString());
		}

		channelInfo = optChannelInfo.get();

		inputOutput.setChannel(channelInfo.getName());
		inputOutput.setChannelUUID(channelInfo.getUuid());

		return channelInfo;
	}

	private void mapGeneration(InputOutput inputOutput, ChannelInfo channelInfo) {
		final GenerationInfo generationInfo;
		if ((inputOutput.getGenerationUUID() == null) && (inputOutput.getGeneration() == null)) {
			generationInfo = channelInfo.getDefaultGeneration();
		} else {
			final String notFoundMessage;
			final Predicate<GenerationInfo> generationFilter;

			if (inputOutput.getGenerationUUID() == null) {
				notFoundMessage = "Unable to find generation with name " + inputOutput.getGeneration();
				generationFilter = (item) -> inputOutput.getGeneration().equalsIgnoreCase(item.getName());
			} else {
				notFoundMessage = "Unable to find generation with UUID " + inputOutput.getGenerationUUID();
				generationFilter = (item) -> inputOutput.getGenerationUUID().equals(item.getUuid());
			}

			Optional<GenerationInfo> optGenerationInfo = channelInfo.getAllowedGenerations().stream()
					.filter(generationFilter).findFirst();

			if (optGenerationInfo.isEmpty()) {
				throw new IllegalArgumentException(notFoundMessage + ". " + channelInfo.getAllowedGenerations());
			}

			generationInfo = optGenerationInfo.get();
		}

		inputOutput.setGeneration(generationInfo.getName());
		inputOutput.setGenerationUUID(generationInfo.getUuid());
	}

	private void mapClusterPlan(InputOutput inputOutput, ParametersResponse parameters) {
		final ClusterPlanTypeInfo clusterPlanInfo;

		if ((inputOutput.getClusterPlanUUID() == null) && (inputOutput.getClusterPlan() == null)) {
			throw new IllegalArgumentException("Neither 'clusterPlan' nor 'clusterPlanUUID' are provided");
		}

		final String notFoundMessage;
		final Predicate<ClusterPlanTypeInfo> clusterPlanFilter;

		if (inputOutput.getClusterPlanUUID() == null) {
			notFoundMessage = "Unable to find clusterPlan with name " + inputOutput.getClusterPlan();
			clusterPlanFilter = (item) -> inputOutput.getClusterPlan().equalsIgnoreCase(item.getName());
		} else {
			notFoundMessage = "Unable to find clusterPlan with UUID " + inputOutput.getClusterPlanUUID();
			clusterPlanFilter = (item) -> inputOutput.getClusterPlanUUID().equals(item.getUuid());
		}

		Optional<ClusterPlanTypeInfo> optClusterPlanInfo = parameters.getClusterPlanTypes().stream()
				.filter(clusterPlanFilter).findFirst();

		if (optClusterPlanInfo.isEmpty()) {
			throw new IllegalArgumentException(notFoundMessage + ". " + parameters.getClusterPlanTypes().toString());
		}

		clusterPlanInfo = optClusterPlanInfo.get();

		inputOutput.setClusterPlan(clusterPlanInfo.getName());
		inputOutput.setClusterPlanUUID(clusterPlanInfo.getUuid());
	}

	private void mapRegion(InputOutput inputOutput, ParametersResponse parameters) {
		final RegionInfo regionInfo;

		if ((inputOutput.getRegionUUID() == null) && (inputOutput.getRegion() == null)) {
			throw new IllegalArgumentException("Neither 'region' nor 'regionUUID' are provided");
		}

		final String notFoundMessage;
		final Predicate<RegionInfo> regionFilter;

		if (inputOutput.getRegionUUID() == null) {
			notFoundMessage = "Unable to find region with name " + inputOutput.getRegion();
			regionFilter = (item) -> inputOutput.getRegion().equalsIgnoreCase(item.getName());
		} else {
			notFoundMessage = "Unable to find region with UUID " + inputOutput.getRegionUUID();
			regionFilter = (item) -> inputOutput.getRegionUUID().equals(item.getUuid());
		}

		Optional<RegionInfo> optRegionInfo = parameters.getRegions().stream().filter(regionFilter).findFirst();

		if (optRegionInfo.isEmpty()) {
			throw new IllegalArgumentException(notFoundMessage + ". " + parameters.getRegions().toString());
		}

		regionInfo = optRegionInfo.get();

		inputOutput.setRegion(regionInfo.getName());
		inputOutput.setRegionUUID(regionInfo.getUuid());
	}

	private static final class InputOutput {
		private String generation;
		private String generationUUID;

		private String region;
		private String regionUUID;

		private String clusterPlan;
		private String clusterPlanUUID;

		private String channel;
		private String channelUUID;

		public String getGeneration() {
			return generation;
		}

		public void setGeneration(String generation) {
			this.generation = generation;
		}

		public String getGenerationUUID() {
			return generationUUID;
		}

		public void setGenerationUUID(String generationUUID) {
			this.generationUUID = generationUUID;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getRegionUUID() {
			return regionUUID;
		}

		public void setRegionUUID(String regionUUID) {
			this.regionUUID = regionUUID;
		}

		public String getClusterPlan() {
			return clusterPlan;
		}

		public void setClusterPlan(String clusterPlan) {
			this.clusterPlan = clusterPlan;
		}

		public String getClusterPlanUUID() {
			return clusterPlanUUID;
		}

		public void setClusterPlanUUID(String clusterPlanUUID) {
			this.clusterPlanUUID = clusterPlanUUID;
		}

		public String getChannel() {
			return channel;
		}

		public void setChannel(String channel) {
			this.channel = channel;
		}

		public String getChannelUUID() {
			return channelUUID;
		}

		public void setChannelUUID(String channelUUID) {
			this.channelUUID = channelUUID;
		}

		@Override
		public String toString() {
			return "InputOutput [generation=" + generation + ", generationUUID=" + generationUUID + ", region=" + region
					+ ", regionUUID=" + regionUUID + ", clusterPlan=" + clusterPlan + ", clusterPlanUUID="
					+ clusterPlanUUID + ", channel=" + channel + ", channelUUID=" + channelUUID + "]";
		}

	}

}
