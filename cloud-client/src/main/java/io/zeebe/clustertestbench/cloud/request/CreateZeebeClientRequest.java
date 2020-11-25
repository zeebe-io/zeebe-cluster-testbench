package io.zeebe.clustertestbench.cloud.request;

import java.util.Collections;
import java.util.List;

public class CreateZeebeClientRequest {

  private static final List<String> PERMISSIONS = Collections.singletonList("zeebe");
  private final String clientName;

  public CreateZeebeClientRequest(String clientName) {
    this.clientName = clientName;
  }

  public String getClientName() {
    return clientName;
  }

  public List<String> getPermissions() {
    return PERMISSIONS;
  }

  @Override
  public String toString() {
    return "CreateClientRequest [clientName=" + clientName + "]";
  }
}
