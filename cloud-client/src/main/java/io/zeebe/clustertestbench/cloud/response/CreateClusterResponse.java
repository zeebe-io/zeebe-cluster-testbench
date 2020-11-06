package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateClusterResponse {
  private String clusterId;

  public String getClusterId() {
    return clusterId;
  }

  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }

  @Override
  public String toString() {
    return "CreateClusterResponse [clusterId=" + clusterId + "]";
  }
}
