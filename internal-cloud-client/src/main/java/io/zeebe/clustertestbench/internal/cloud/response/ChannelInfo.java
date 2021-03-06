package io.zeebe.clustertestbench.internal.cloud.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelInfo {

  private List<GenerationInfo> allowedGenerations = new ArrayList<>();

  private GenerationInfo defaultGeneration;

  @JsonAlias("isDefault")
  private boolean isDefault;

  private String name;
  private String uuid;

  public List<GenerationInfo> getAllowedGenerations() {
    return allowedGenerations;
  }

  public void setAllowedGenerations(final List<GenerationInfo> allowedGenerations) {
    this.allowedGenerations = allowedGenerations;
  }

  public GenerationInfo getDefaultGeneration() {
    return defaultGeneration;
  }

  public void setDefaultGeneration(final GenerationInfo defaultGeneration) {
    this.defaultGeneration = defaultGeneration;
  }

  public boolean isDefault() {
    return isDefault;
  }

  @JsonAlias("isDefault")
  public void setDefault(final boolean isDefault) {
    this.isDefault = isDefault;
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
    return "ChannelInfo [allowedGenerations="
        + allowedGenerations
        + ", defaultGenerations="
        + defaultGeneration
        + ", isDefault="
        + isDefault
        + ", name="
        + name
        + ", uuid="
        + uuid
        + "]";
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GenerationInfo {

    private String name;
    private String uuid;
    private Map<String, String> versions;

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

    public Map<String, String> getVersions() {
      return versions;
    }

    public void setVersions(final Map<String, String> versions) {
      this.versions = versions;
    }

    @Override
    public String toString() {
      return "GenerationInfo [name=" + name + ", uuid=" + uuid + ", versions=" + versions + "]";
    }
  }
}
