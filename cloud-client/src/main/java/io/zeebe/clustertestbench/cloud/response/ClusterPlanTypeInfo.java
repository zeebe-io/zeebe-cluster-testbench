package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterPlanTypeInfo {

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
    return "ClusterPlanTypeInfo [internal=" + internal + ", name=" + name + ", uuid=" + uuid + "]";
  }
}
