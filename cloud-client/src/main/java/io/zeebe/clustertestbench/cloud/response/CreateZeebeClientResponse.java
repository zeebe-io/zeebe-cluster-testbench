package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateZeebeClientResponse {

  private String clientId;
  private String clientSecret;

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  @Override
  public String toString() {
    return "CreateClientResponse [clientId=" + clientId + ", clientSecret=" + clientSecret + "]";
  }
}
