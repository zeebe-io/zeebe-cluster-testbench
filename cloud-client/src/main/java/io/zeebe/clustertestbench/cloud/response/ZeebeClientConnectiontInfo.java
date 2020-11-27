package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZeebeClientConnectiontInfo {

  private String name;

  @JsonAlias("ZEEBE_ADDRESS")
  private String zeebeAddress;

  @JsonAlias("ZEEBE_CLIENT_ID")
  private String zeebeClientId;

  @JsonAlias("ZEEBE_AUTHORIZATION_SERVER_URL")
  private String zeebeAuthorizationServerUrl;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getZeebeAddress() {
    return zeebeAddress;
  }

  public void setZeebeAddress(final String zeebeAddress) {
    this.zeebeAddress = zeebeAddress;
  }

  public String getZeebeClientId() {
    return zeebeClientId;
  }

  public void setZeebeClientId(final String zeebeClientId) {
    this.zeebeClientId = zeebeClientId;
  }

  public String getZeebeAuthorizationServerUrl() {
    return zeebeAuthorizationServerUrl;
  }

  public void setZeebeAuthorizationServerUrl(final String zeebeAuthorizationServerUrl) {
    this.zeebeAuthorizationServerUrl = zeebeAuthorizationServerUrl;
  }

  public String getZeebeAudience() {
    return zeebeAddress.substring(0, zeebeAddress.lastIndexOf(":"));
  }

  @Override
  public String toString() {
    return "ZeebeClientConnectiontInfo [name="
        + name
        + ", zeebeAddress="
        + zeebeAddress
        + ", zeebeClientId="
        + zeebeClientId
        + ", zeebeAuthorizationServerUrl="
        + zeebeAuthorizationServerUrl
        + "]";
  }
}
