package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionInfo {

  private String name;
  private String region;
  private String uuid;
  private String zone;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getZone() {
    return zone;
  }

  public void setZone(String zone) {
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
