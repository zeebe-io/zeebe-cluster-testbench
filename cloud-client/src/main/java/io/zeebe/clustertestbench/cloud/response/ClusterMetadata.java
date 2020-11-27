package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterMetadata {

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
