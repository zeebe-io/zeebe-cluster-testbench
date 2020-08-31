package io.zeebe.clustertestbench.cloud.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelInfo {

	private List<GenerationInfo> allowedGenerations;

	private GenerationInfo defaultGeneration;

	@JsonAlias("isDefault")
	private boolean isDefault;
	private String name;
	private String uuid;

	public List<GenerationInfo> getAllowedGenerations() {
		return allowedGenerations;
	}

	public void setAllowedGenerations(List<GenerationInfo> allowedGenerations) {
		this.allowedGenerations = allowedGenerations;
	}

	public GenerationInfo getDefaultGeneration() {
		return defaultGeneration;
	}

	public void setDefaultGeneration(GenerationInfo defaultGeneration) {
		this.defaultGeneration = defaultGeneration;
	}

	public boolean isDefault() {
		return isDefault;
	}

	@JsonAlias("isDefault")
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return "ChannelInfo [allowedGenerations=" + allowedGenerations + ", defaultGenerations=" + defaultGeneration
				+ ", isDefault=" + isDefault + ", name=" + name + ", uuid=" + uuid + "]";
	}

}
