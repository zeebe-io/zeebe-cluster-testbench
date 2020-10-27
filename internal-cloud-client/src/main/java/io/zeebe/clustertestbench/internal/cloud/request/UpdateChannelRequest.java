package io.zeebe.clustertestbench.internal.cloud.request;

import java.util.List;

public class UpdateChannelRequest {

	private final String name;
	private final boolean isDefault;
	private final String defaultgenerationId;
	private final List<String> allowedGeneerationIds;

	public UpdateChannelRequest(String name, boolean isDefault, String defaultgenerationId,
			List<String> allowedGeneerationIds) {
		this.name = name;
		this.isDefault = isDefault;
		this.defaultgenerationId = defaultgenerationId;
		this.allowedGeneerationIds = allowedGeneerationIds;
	}

	public String getName() {
		return name;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public String getDefaultgenerationId() {
		return defaultgenerationId;
	}

	public List<String> getAllowedGeneerationIds() {
		return allowedGeneerationIds;
	}

	@Override
	public String toString() {
		return "UpdateChannelRequest [name=" + name + ", isDefault=" + isDefault + ", defaultgenerationId="
				+ defaultgenerationId + ", allowedGeneerationIds=" + allowedGeneerationIds + "]";
	}

}
