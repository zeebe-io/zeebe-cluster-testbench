package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.zeebe.clustertestbench.cloud.response.ClusterInfo.K8sContextInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChannelInfo {

    private List<GenerationInfo> allowedGenerations = new ArrayList();

    private GenerationInfo defaultGeneration;

    @JsonAlias("isDefault")
    private boolean isDefault;

    private String name;
    private String uuid;

    public List<GenerationInfo> getAllowedGenerations() {
      return allowedGenerations;
    }

    public void setAllowedGenerations(final List<GenerationInfo> allowedGenerations) {
      this.allowedGenerations = allowedGenerations;
    }

    public GenerationInfo getDefaultGeneration() {
      return defaultGeneration;
    }

    public void setDefaultGeneration(final GenerationInfo defaultGeneration) {
      this.defaultGeneration = defaultGeneration;
    }

    public boolean isDefault() {
      return isDefault;
    }

    @JsonAlias("isDefault")
    public void setDefault(final boolean isDefault) {
      this.isDefault = isDefault;
    }

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public String getUuid() {
      return uuid;
    }

    public void setUuid(final String uuid) {
      this.uuid = uuid;
    }

    @Override
    public String toString() {
      return "ChannelInfo [allowedGenerations="
          + allowedGenerations
          + ", defaultGenerations="
          + defaultGeneration
          + ", isDefault="
          + isDefault
          + ", name="
          + name
          + ", uuid="
          + uuid
          + "]";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GenerationInfo {

      private String name;
      private String uuid;
      private Map<String, String> versions;

      public String getName() {
        return name;
      }

      public void setName(final String name) {
        this.name = name;
      }

      public String getUuid() {
        return uuid;
      }

      public void setUuid(final String uuid) {
        this.uuid = uuid;
      }

      public Map<String, String> getVersions() {
        return versions;
      }

      public void setVersions(final Map<String, String> versions) {
        this.versions = versions;
      }

      @Override
      public String toString() {
        return "GenerationInfo [name=" + name + ", uuid=" + uuid + ", versions=" + versions + "]";
      }
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ClusterPlanTypeInfo {

    private String description;
    private boolean internal;
    private String name;
    private String uuid;
    private K8sContextInfo k8sContext;

    public String getDescription() {
      return description;
    }

    public void setDescription(final String description) {
      this.description = description;
    }

    public boolean isInternal() {
      return internal;
    }

    public void setInternal(final boolean internal) {
      this.internal = internal;
    }

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public String getUuid() {
      return uuid;
    }

    public void setUuid(final String uuid) {
      this.uuid = uuid;
    }

    public K8sContextInfo getK8sContext() {
      return k8sContext;
    }

    public void setK8sContext(final K8sContextInfo k8sContext) {
      this.k8sContext = k8sContext;
    }

    @Override
    public String toString() {
      return "ClusterPlanTypeInfo [internal="
          + internal
          + ", name="
          + name
          + ", uuid="
          + uuid
          + "]";
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class RegionInfo {

    private String name;
    private String region;
    private String uuid;
    private String zone;

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public String getRegion() {
      return region;
    }

    public void setRegion(final String region) {
      this.region = region;
    }

    public String getUuid() {
      return uuid;
    }

    public void setUuid(final String uuid) {
      this.uuid = uuid;
    }

    public String getZone() {
      return zone;
    }

    public void setZone(final String zone) {
      this.zone = zone;
    }

    @Override
    public String toString() {
      return "RegionInfo [name="
          + name
          + ", region="
          + region
          + ", uuid="
          + uuid
          + ", zone="
          + zone
          + "]";
    }
  }
}
