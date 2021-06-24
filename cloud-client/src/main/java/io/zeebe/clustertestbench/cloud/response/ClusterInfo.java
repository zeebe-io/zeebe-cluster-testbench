package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterInfo {

  private ClusterPlanTypeInfo planType;

  private K8sContextInfo k8sContext;

  private String uuid;
  private String ownerId;
  private String name;

  private boolean internal;

  private GenerationInfo generation;

  private ChannelInfo channel;

  private ClusterStatus status;

  private ClusterMetadata metadata;

  public ClusterPlanTypeInfo getPlanType() {
    return planType;
  }

  public void setPlanType(final ClusterPlanTypeInfo planType) {
    this.planType = planType;
  }

  public K8sContextInfo getK8sContext() {
    return k8sContext;
  }

  public void setK8sContext(final K8sContextInfo k8sContext) {
    this.k8sContext = k8sContext;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(final String uuid) {
    this.uuid = uuid;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(final String ownerId) {
    this.ownerId = ownerId;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public boolean isInternal() {
    return internal;
  }

  public void setInternal(final boolean internal) {
    this.internal = internal;
  }

  public GenerationInfo getGeneration() {
    return generation;
  }

  public void setGeneration(final GenerationInfo generation) {
    this.generation = generation;
  }

  public ChannelInfo getChannel() {
    return channel;
  }

  public void setChannel(final ChannelInfo channel) {
    this.channel = channel;
  }

  public ClusterStatus getStatus() {
    return status;
  }

  public void setStatus(final ClusterStatus status) {
    this.status = status;
  }

  public ClusterMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(final ClusterMetadata metadata) {
    this.metadata = metadata;
  }

  @Override
  public String toString() {
    return "ClusterInfo [planType="
        + planType
        + ", k8sContext="
        + k8sContext
        + ", uuid="
        + uuid
        + ", ownerId="
        + ownerId
        + ", name="
        + name
        + ", internal="
        + internal
        + ", generation="
        + generation
        + ", channel="
        + channel
        + ", status="
        + status
        + ", metadata="
        + metadata
        + "]";
  }

  /* TODO
   * - field "spec"
   **/

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ClusterPlanTypeInfo {

    private String description;
    private boolean internal;
    private String name;
    private String uuid;

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
  public static class K8sContextInfo {

    private String uuid;
    private String name;
    private String region;
    private String zone;

    public String getUuid() {
      return uuid;
    }

    public void setUuid(final String uuid) {
      this.uuid = uuid;
    }

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

    public String getZone() {
      return zone;
    }

    public void setZone(final String zone) {
      this.zone = zone;
    }

    @Override
    public String toString() {
      return "K8sContextInfo [uuid="
          + uuid
          + ", name="
          + name
          + ", region="
          + region
          + ", zone="
          + zone
          + "]";
    }
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

    public void setDefaultGeneration(GenerationInfo defaultGeneration) {
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
  public static class ClusterStatus {

    private String operateStatus;
    private String operateUrl;
    private String ready;
    private String zeebeStatus;
    private String zeebeUrl;

    public String getOperateStatus() {
      return operateStatus;
    }

    public void setOperateStatus(final String operateStatus) {
      this.operateStatus = operateStatus;
    }

    public String getOperateUrl() {
      return operateUrl;
    }

    public void setOperateUrl(final String operateUrl) {
      this.operateUrl = operateUrl;
    }

    public String getReady() {
      return ready;
    }

    public void setReady(final String ready) {
      this.ready = ready;
    }

    public String getZeebeStatus() {
      return zeebeStatus;
    }

    public void setZeebeStatus(final String zeebeStatus) {
      this.zeebeStatus = zeebeStatus;
    }

    public String getZeebeUrl() {
      return zeebeUrl;
    }

    public void setZeebeUrl(final String zeebeUrl) {
      this.zeebeUrl = zeebeUrl;
    }

    @Override
    public String toString() {
      return "ClusterStatus [operateStatus="
          + operateStatus
          + ", operateUrl="
          + operateUrl
          + ", ready="
          + ready
          + ", zeebeStatus="
          + zeebeStatus
          + ", zeebeUrl="
          + zeebeUrl
          + "]";
    }

    /*
     * TODO - other fields - map status fields to enums
     */

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ClusterMetadata {

    private String creationTimestamp;
    private int generation;
    private String name;
    private String resourceVersion;
    private String selfLink;
    private String uid;

    public String getCreationTimestamp() {
      return creationTimestamp;
    }

    public void setCreationTimestamp(final String creationTimestamp) {
      this.creationTimestamp = creationTimestamp;
    }

    public int getGeneration() {
      return generation;
    }

    public void setGeneration(final int generation) {
      this.generation = generation;
    }

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public String getResourceVersion() {
      return resourceVersion;
    }

    public void setResourceVersion(final String resourceVersion) {
      this.resourceVersion = resourceVersion;
    }

    public String getSelfLink() {
      return selfLink;
    }

    public void setSelfLink(final String selfLink) {
      this.selfLink = selfLink;
    }

    public String getUid() {
      return uid;
    }

    public void setUid(final String uid) {
      this.uid = uid;
    }

    @Override
    public String toString() {
      return "ClusterMetadata [creationTimestamp="
          + creationTimestamp
          + ", generation="
          + generation
          + ", name="
          + name
          + ", resourceVersion="
          + resourceVersion
          + ", selfLink="
          + selfLink
          + ", uid="
          + uid
          + "]";
    }
  }
}
