package io.zeebe.clustertestbench.internal.cloud.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateChannelRequest {

	private final String name;
	private final boolean isDefault;
	private final String defaultGenerationId;
	private final List<String> allowedGenerationIds;

	public UpdateChannelRequest(String name, boolean isDefault, String defaultgenerationId,
			List<String> allowedGenerationIds) {
		this.name = name;
		this.isDefault = isDefault;
		this.defaultGenerationId = defaultgenerationId;
		this.allowedGenerationIds = allowedGenerationIds;
	}

	public String getName() {
		return name;
	}

	@JsonProperty("isDefault")
	public boolean isDefault() {
		return isDefault;
	}

	public String getDefaultGenerationId() {
		return defaultGenerationId;
	}

	public List<String> getAllowedGenerationIds() {
		return allowedGenerationIds;
	}

	@Override
	public String toString() {
		return "UpdateChannelRequest [name=" + name + ", isDefault=" + isDefault + ", defaultgenerationId="
				+ defaultGenerationId + ", allowedGeneerationIds=" + allowedGenerationIds + "]";
	}

}
