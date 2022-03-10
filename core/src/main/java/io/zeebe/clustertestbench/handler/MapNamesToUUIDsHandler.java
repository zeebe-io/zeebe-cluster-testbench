package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.response.ParametersResponse;
import io.zeebe.clustertestbench.cloud.response.ParametersResponse.ChannelInfo;
import io.zeebe.clustertestbench.cloud.response.ParametersResponse.ChannelInfo.GenerationInfo;
import io.zeebe.clustertestbench.cloud.response.ParametersResponse.ClusterPlanTypeInfo;
import io.zeebe.clustertestbench.cloud.response.ParametersResponse.RegionInfo;
import io.zeebe.clustertestbench.util.StringLookup;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapNamesToUUIDsHandler implements JobHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MapNamesToUUIDsHandler.class);

  private final CloudAPIClient cloudClient;

  public MapNamesToUUIDsHandler(final CloudAPIClient cloudAPIClient) {
    cloudClient = cloudAPIClient;
  }

  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {
    LOGGER.info("Mapping names to UUIDs and vice versa");
    final InputOutput inputOutput = job.getVariablesAsType(InputOutput.class);

    LOGGER.info("Input: " + inputOutput);

    final ParametersResponse parameters = cloudClient.getParameters();

    final ChannelInfo channelInfo = mapChannel(inputOutput, parameters);

    final RegionInfo regionInfo = mapRegion(inputOutput, parameters);

    mapGeneration(inputOutput, channelInfo);

    mapClusterPlan(inputOutput, parameters, regionInfo);

    LOGGER.info("Output: " + inputOutput);
    client.newCompleteCommand(job.getKey()).variables(inputOutput).send();
  }

  private ChannelInfo mapChannel(
      final InputOutput inputOutput, final ParametersResponse parameters) {
    final ChannelInfo channelInfo;

    if ((inputOutput.getChannelUUID() == null) && (inputOutput.getChannel() == null)) {
      throw new IllegalArgumentException("Neither 'generation' nor 'generationUUID' are provided");
    }

    final StringLookup<ChannelInfo> channelLookup;
    if (inputOutput.getChannelUUID() == null) {
      channelLookup =
          new StringLookup<>(
              "channel",
              inputOutput.getChannel(),
              parameters.getChannels(),
              ChannelInfo::getName,
              true);
    } else {
      channelLookup =
          new StringLookup<>(
              "channel",
              inputOutput.getChannelUUID(),
              parameters.getChannels(),
              ChannelInfo::getUuid,
              false);
    }

    channelInfo = channelLookup.lookup().getOrElseThrow(msg -> new IllegalArgumentException(msg));

    inputOutput.setChannel(channelInfo.getName());
    inputOutput.setChannelUUID(channelInfo.getUuid());

    return channelInfo;
  }

  private void mapGeneration(final InputOutput inputOutput, final ChannelInfo channelInfo) {
    final GenerationInfo generationInfo;
    if ((inputOutput.getGenerationUUID() == null) && (inputOutput.getGeneration() == null)) {
      generationInfo = channelInfo.getDefaultGeneration();
    } else {

      final StringLookup<GenerationInfo> generationLoookup;
      if (inputOutput.getGenerationUUID() == null) {
        generationLoookup =
            new StringLookup<>(
                "generation",
                inputOutput.getGeneration(),
                channelInfo.getAllowedGenerations(),
                GenerationInfo::getName,
                true);
      } else {
        generationLoookup =
            new StringLookup<>(
                "generation",
                inputOutput.getGenerationUUID(),
                channelInfo.getAllowedGenerations(),
                GenerationInfo::getUuid,
                false);
      }

      generationInfo =
          generationLoookup.lookup().getOrElseThrow(msg -> new IllegalArgumentException(msg));
    }

    inputOutput.setGeneration(generationInfo.getName());
    inputOutput.setGenerationUUID(generationInfo.getUuid());
  }

  private void mapClusterPlan(
      final InputOutput inputOutput,
      final ParametersResponse parameters,
      final RegionInfo regionInfo) {
    final ClusterPlanTypeInfo clusterPlanInfo;

    if ((inputOutput.getClusterPlanUUID() == null) && (inputOutput.getClusterPlan() == null)) {
      throw new IllegalArgumentException(
          "Neither 'clusterPlan' nor 'clusterPlanUUID' are provided");
    }

    final StringLookup<ClusterPlanTypeInfo> clusterPlanLookup;
    if (inputOutput.getClusterPlanUUID() == null) {

      final var clusterPlansInTheRegion =
          parameters.getClusterPlanTypes().stream()
              .filter(plan -> regionInfo.getUuid().equals(plan.getK8sContext().uuid()))
              .collect(Collectors.toList());

      clusterPlanLookup =
          new StringLookup<>(
              "clusterPlan",
              inputOutput.getClusterPlan(),
              clusterPlansInTheRegion,
              ClusterPlanTypeInfo::getName,
              true);
    } else {
      clusterPlanLookup =
          new StringLookup<>(
              "clusterPlan",
              inputOutput.getClusterPlanUUID(),
              parameters.getClusterPlanTypes(),
              ClusterPlanTypeInfo::getUuid,
              false);
    }

    clusterPlanInfo =
        clusterPlanLookup.lookup().getOrElseThrow(msg -> new IllegalArgumentException(msg));

    inputOutput.setClusterPlan(clusterPlanInfo.getName());
    inputOutput.setClusterPlanUUID(clusterPlanInfo.getUuid());
  }

  private RegionInfo mapRegion(final InputOutput inputOutput, final ParametersResponse parameters) {
    final RegionInfo regionInfo;

    if ((inputOutput.getRegionUUID() == null) && (inputOutput.getRegion() == null)) {
      throw new IllegalArgumentException("Neither 'region' nor 'regionUUID' are provided");
    }

    final StringLookup<RegionInfo> regionLookup;
    if (inputOutput.getRegionUUID() == null) {
      regionLookup =
          new StringLookup<>(
              "region",
              inputOutput.getRegion(),
              parameters.getRegions(),
              RegionInfo::getName,
              true);
    } else {
      regionLookup =
          new StringLookup<>(
              "region",
              inputOutput.getRegionUUID(),
              parameters.getRegions(),
              RegionInfo::getUuid,
              false);
    }

    regionInfo = regionLookup.lookup().getOrElseThrow(msg -> new IllegalArgumentException(msg));

    inputOutput.setRegion(regionInfo.getName());
    inputOutput.setRegionUUID(regionInfo.getUuid());
    return regionInfo;
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

    public void setGeneration(final String generation) {
      this.generation = generation;
    }

    public String getGenerationUUID() {
      return generationUUID;
    }

    public void setGenerationUUID(final String generationUUID) {
      this.generationUUID = generationUUID;
    }

    public String getRegion() {
      return region;
    }

    public void setRegion(final String region) {
      this.region = region;
    }

    public String getRegionUUID() {
      return regionUUID;
    }

    public void setRegionUUID(final String regionUUID) {
      this.regionUUID = regionUUID;
    }

    public String getClusterPlan() {
      return clusterPlan;
    }

    public void setClusterPlan(final String clusterPlan) {
      this.clusterPlan = clusterPlan;
    }

    public String getClusterPlanUUID() {
      return clusterPlanUUID;
    }

    public void setClusterPlanUUID(final String clusterPlanUUID) {
      this.clusterPlanUUID = clusterPlanUUID;
    }

    public String getChannel() {
      return channel;
    }

    public void setChannel(final String channel) {
      this.channel = channel;
    }

    public String getChannelUUID() {
      return channelUUID;
    }

    public void setChannelUUID(final String channelUUID) {
      this.channelUUID = channelUUID;
    }

    @Override
    public String toString() {
      return "InputOutput [generation="
          + generation
          + ", generationUUID="
          + generationUUID
          + ", region="
          + region
          + ", regionUUID="
          + regionUUID
          + ", clusterPlan="
          + clusterPlan
          + ", clusterPlanUUID="
          + clusterPlanUUID
          + ", channel="
          + channel
          + ", channelUUID="
          + channelUUID
          + "]";
    }
  }
}
