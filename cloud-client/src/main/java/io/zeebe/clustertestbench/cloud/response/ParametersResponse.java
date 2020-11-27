package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParametersResponse {

  private List<ChannelInfo> channels;

  private List<ClusterPlanTypeInfo> clusterPlanTypes;

  private List<RegionInfo> regions;

  public List<ChannelInfo> getChannels() {
    return channels;
  }

  public void setChannels(final List<ChannelInfo> channels) {
    this.channels = channels;
  }

  public List<ClusterPlanTypeInfo> getClusterPlanTypes() {
    return clusterPlanTypes;
  }

  public void setClusterPlanTypes(final List<ClusterPlanTypeInfo> clusterPlanTypes) {
    this.clusterPlanTypes = clusterPlanTypes;
  }

  public List<RegionInfo> getRegions() {
    return regions;
  }

  public void setRegions(final List<RegionInfo> regions) {
    this.regions = regions;
  }

  @Override
  public String toString() {
    return "ParmetersResponse [channels="
        + channels
        + ", clusterPlanTypes="
        + clusterPlanTypes
        + ", regions="
        + regions
        + "]";
  }
}
