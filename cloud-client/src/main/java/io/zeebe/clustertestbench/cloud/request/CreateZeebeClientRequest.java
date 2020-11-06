package io.zeebe.clustertestbench.cloud.request;

public class CreateZeebeClientRequest {

  private final String clientName;

  public CreateZeebeClientRequest(String clientName) {
    this.clientName = clientName;
  }

  public String getClientName() {
    return clientName;
  }

  @Override
  public String toString() {
    return "CreateClientRequest [clientName=" + clientName + "]";
  }
}
