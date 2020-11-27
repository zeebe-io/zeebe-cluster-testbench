package io.zeebe.clustertestbench.internal.cloud.request;

import java.util.List;
import java.util.Map;

public class CreateGenerationRequest {

  private final String name;
  private final Map<String, String> versions;
  private final List<String> upgradeableFrom;

  public CreateGenerationRequest(
      final String name, final Map<String, String> versions, final List<String> upgradeableFrom) {
    this.name = name;
    this.versions = versions;
    this.upgradeableFrom = upgradeableFrom;
  }

  public String getName() {
    return name;
  }

  public Map<String, String> getVersions() {
    return versions;
  }

  public List<String> getUpgradeableFrom() {
    return upgradeableFrom;
  }

  @Override
  public String toString() {
    return "CreateGenerationRequest [name="
        + name
        + ", versions="
        + versions
        + ", upgradeableFrom="
        + upgradeableFrom
        + "]";
  }
}
