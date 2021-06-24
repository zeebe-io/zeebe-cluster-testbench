package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterInfo {

  private String uuid;
  private String name;
  private String created;
  private ClusterPlanTypeInfo planType;
  private K8sContextInfo k8sContext;
  private GenerationInfo generation;
  private ChannelInfo channel;
  private ClusterStatus status;
  private Links links;

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

  public String getCreated() {
    return created;
  }

  public void setCreated(final String created) {
    this.created = created;
  }

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

  public Links getLinks() {
    return links;
  }

  public void setLinks(final Links links) {
    this.links = links;
  }

  @Override
  public String toString() {
    return "ClusterInfo [uuid="
        + uuid
        + ", name="
        + name
        + ", created="
        + created
        + ", planType="
        + planType
        + ", k8sContext="
        + k8sContext
        + ", generation="
        + generation
        + ", channel="
        + channel
        + ", status="
        + status
        + ", links="
        + links
        + "]";
  }

  /* TODO
   * - field "spec"
   **/

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ClusterPlanTypeInfo {

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
      return "ClusterPlanTypeInfo [uuid=" + uuid + ", name=" + name + "]";
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
      return "K8sContextInfo [uuid=" + uuid + ", name=" + name + "]";
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GenerationInfo {

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
      return "GenerationInfo [uuid=" + uuid + ", name=" + name + "]";
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChannelInfo {

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
      return "ChannelInfo [uuid=" + uuid + ", name=" + name + "]";
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ClusterStatus {

    private String ready;
    private String zeebeStatus;
    private String operateStatus;
    private String operateUrl; // fixme: has been removed but is still used

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

    @Override
    public String toString() {
      return "ClusterStatus [ready="
          + ready
          + ", zeebeStatus="
          + zeebeStatus
          + ", operateStatus="
          + operateStatus
          + ", operateUrl="
          + operateUrl
          + "]";
    }

    /*
     * TODO - other fields - map status fields to enums
     */

  }

  public static class Links {

    private String zeebe;
    private String operate;
    private String tasklist;

    public String getZeebe() {
      return zeebe;
    }

    public void setZeebe(final String zeebe) {
      this.zeebe = zeebe;
    }

    public String getOperate() {
      return operate;
    }

    public void setOperate(final String operate) {
      this.operate = operate;
    }

    public String getTasklist() {
      return tasklist;
    }

    public void setTasklist(final String tasklist) {
      this.tasklist = tasklist;
    }

    @Override
    public String toString() {
      return "Links [" + "zeebe=" + zeebe + ", operate=" + operate + ", tasklist=" + tasklist + ']';
    }
  }
}
