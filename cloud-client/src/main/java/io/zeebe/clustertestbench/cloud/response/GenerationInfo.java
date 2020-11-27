package io.zeebe.clustertestbench.cloud.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerationInfo {

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
