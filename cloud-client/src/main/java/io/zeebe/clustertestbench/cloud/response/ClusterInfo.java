package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

}
