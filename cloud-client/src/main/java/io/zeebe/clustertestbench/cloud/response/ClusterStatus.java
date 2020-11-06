package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterStatus {

  private String operateStatus;
  private String operateUrl;
  private String ready;
  private String zeebeStatus;
  private String zeebeUrl;

  public String getOperateStatus() {
    return operateStatus;
  }

  public void setOperateStatus(String operateStatus) {
    this.operateStatus = operateStatus;
  }

  public String getOperateUrl() {
    return operateUrl;
  }

  public void setOperateUrl(String operateUrl) {
    this.operateUrl = operateUrl;
  }

  public String getReady() {
    return ready;
  }

  public void setReady(String ready) {
    this.ready = ready;
  }

  public String getZeebeStatus() {
    return zeebeStatus;
  }

  public void setZeebeStatus(String zeebeStatus) {
    this.zeebeStatus = zeebeStatus;
  }

  public String getZeebeUrl() {
    return zeebeUrl;
  }

  public void setZeebeUrl(String zeebeUrl) {
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
