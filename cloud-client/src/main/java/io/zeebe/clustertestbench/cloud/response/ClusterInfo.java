package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterInfo {

  private ClusterPlanTypeInfo planType;

  private K8sContextInfo k8sContext;

  private String uuid;
  private String name;

  private GenerationInfo generation;

  private ChannelInfo channel;

  private ClusterStatus status;

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

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
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

  @Override
  public String toString() {
    return "ClusterInfo [planType="
        + planType
        + ", k8sContext="
        + k8sContext
        + ", uuid="
        + uuid
        + ", name="
        + name
        + ", generation="
        + generation
        + ", channel="
        + channel
        + ", status="
        + status
        + "]";
  }

  /* TODO
   * - field "spec"
   **/

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ClusterPlanTypeInfo {

    private String name;
    private String uuid;

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
      return "ClusterPlanTypeInfo [name="
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

    @Override
    public String toString() {
      return "K8sContextInfo [uuid="
          + uuid
          + ", name="
          + name
          + "]";
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GenerationInfo {

    private String name;
    private String uuid;

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
      return "GenerationInfo [name=" + name + ", uuid=" + uuid + "]";
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChannelInfo {

    private String name;
    private String uuid;

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
      return "ChannelInfo [name="
          + name
          + ", uuid="
          + uuid
          + "]";
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ClusterStatus {

    private String operateStatus;
    private String operateUrl; // fixme: has been removed but is still used
    private String ready;
    private String zeebeStatus;

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
          + "]";
    }

    /*
     * TODO - other fields - map status fields to enums
     */

  }

}
