package io.zeebe.clustertestbench.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.zeebe.clustertestbench.cloud.CloudAPIClient;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.ParametersChannelInfo;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.ParametersClusterPlanTypeInfo;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.ParametersGenerationInfo;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.ParametersRegionInfo;
import io.zeebe.clustertestbench.cloud.CloudAPIClient.ParametersResponse;
import io.zeebe.clustertestbench.util.StringLookup;
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

    final ParametersChannelInfo channelInfo = mapChannel(inputOutput, parameters);

    mapRegion(inputOutput, parameters);

    mapGeneration(inputOutput, channelInfo);

    mapClusterPlan(inputOutput, parameters);

    LOGGER.info("Output: " + inputOutput);
    client.newCompleteCommand(job.getKey()).variables(inputOutput).send();
  }

  private ParametersChannelInfo mapChannel(
      final InputOutput inputOutput, final ParametersResponse parameters) {
    final ParametersChannelInfo channelInfo;

    if ((inputOutput.getChannelUUID() == null) && (inputOutput.getChannel() == null)) {
      throw new IllegalArgumentException("Neither 'channel' nor 'channelUUID' are provided");
    }

    final StringLookup<ParametersChannelInfo> channelLookup;
    if (inputOutput.getChannelUUID() == null) {
      channelLookup =
          new StringLookup<>(
              "channel",
              inputOutput.getChannel(),
              parameters.channels(),
              ParametersChannelInfo::name,
              true);
    } else {
      channelLookup =
          new StringLookup<>(
              "channel",
              inputOutput.getChannelUUID(),
              parameters.channels(),
              ParametersChannelInfo::uuid,
              false);
    }

    channelInfo = channelLookup.lookup().getOrElseThrow(msg -> new IllegalArgumentException(msg));

    inputOutput.setChannel(channelInfo.name());
    inputOutput.setChannelUUID(channelInfo.uuid());

    return channelInfo;
  }

  private void mapGeneration(
      final InputOutput inputOutput, final ParametersChannelInfo channelInfo) {
    final ParametersGenerationInfo generationInfo;
    if ((inputOutput.getGenerationUUID() == null) && (inputOutput.getGeneration() == null)) {
      generationInfo = channelInfo.defaultGeneration();
    } else {

      final StringLookup<ParametersGenerationInfo> generationLoookup;
      if (inputOutput.getGenerationUUID() == null) {
        generationLoookup =
            new StringLookup<>(
                "generation",
                inputOutput.getGeneration(),
                channelInfo.allowedGenerations(),
                ParametersGenerationInfo::name,
                true);
      } else {
        generationLoookup =
            new StringLookup<>(
                "generation",
                inputOutput.getGenerationUUID(),
                channelInfo.allowedGenerations(),
                ParametersGenerationInfo::uuid,
                false);
      }

      generationInfo =
          generationLoookup.lookup().getOrElseThrow(msg -> new IllegalArgumentException(msg));
    }

    inputOutput.setGeneration(generationInfo.name());
    inputOutput.setGenerationUUID(generationInfo.uuid());
  }

  private void mapClusterPlan(final InputOutput inputOutput, final ParametersResponse parameters) {
    final ParametersClusterPlanTypeInfo clusterPlanInfo;

    if ((inputOutput.getClusterPlanUUID() == null) && (inputOutput.getClusterPlan() == null)) {
      throw new IllegalArgumentException(
          "Neither 'clusterPlan' nor 'clusterPlanUUID' are provided");
    }

    final StringLookup<ParametersClusterPlanTypeInfo> clusterPlanLookup;
    if (inputOutput.getClusterPlanUUID() == null) {
      clusterPlanLookup =
          new StringLookup<>(
              "clusterPlan",
              inputOutput.getClusterPlan(),
              parameters.clusterPlanTypes(),
              ParametersClusterPlanTypeInfo::name,
              true);
    } else {
      clusterPlanLookup =
          new StringLookup<>(
              "clusterPlan",
              inputOutput.getClusterPlanUUID(),
              parameters.clusterPlanTypes(),
              ParametersClusterPlanTypeInfo::uuid,
              false);
    }

    clusterPlanInfo =
        clusterPlanLookup.lookup().getOrElseThrow(msg -> new IllegalArgumentException(msg));

    inputOutput.setClusterPlan(clusterPlanInfo.name());
    inputOutput.setClusterPlanUUID(clusterPlanInfo.uuid());
  }

  private ParametersRegionInfo mapRegion(
      final InputOutput inputOutput, final ParametersResponse parameters) {
    final ParametersRegionInfo regionInfo;

    if ((inputOutput.getRegionUUID() == null) && (inputOutput.getRegion() == null)) {
      throw new IllegalArgumentException("Neither 'region' nor 'regionUUID' are provided");
    }

    final StringLookup<ParametersRegionInfo> regionLookup;
    if (inputOutput.getRegionUUID() == null) {
      regionLookup =
          new StringLookup<>(
              "region",
              inputOutput.getRegion(),
              parameters.regions(),
              ParametersRegionInfo::name,
              true);
    } else {
      regionLookup =
          new StringLookup<>(
              "region",
              inputOutput.getRegionUUID(),
              parameters.regions(),
              ParametersRegionInfo::name,
              false);
    }

    regionInfo = regionLookup.lookup().getOrElseThrow(msg -> new IllegalArgumentException(msg));

    inputOutput.setRegion(regionInfo.name());
    inputOutput.setRegionUUID(regionInfo.uuid());
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
