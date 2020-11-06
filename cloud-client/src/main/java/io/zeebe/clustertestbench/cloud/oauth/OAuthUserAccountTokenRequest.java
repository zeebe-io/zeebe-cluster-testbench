package io.zeebe.clustertestbench.cloud.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthUserAccountTokenRequest {

	private final String audience;
	private final String clientId;
	private final String clientSecret;
	private final String grantType;

	private final String username;
	private final String password;

	public OAuthUserAccountTokenRequest(String audience, String clientId, String clientSecret, String grantType,
			String username, String password) {
		this.audience = audience;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.grantType = grantType;
		this.username = username;
		this.password = password;
	}

	public String getAudience() {
		return audience;
	}

	@JsonProperty(value = "client_id")
	public String getClientId() {
		return clientId;
	}

	@JsonProperty(value = "client_secret")
	public String getClientSecret() {
		return clientSecret;
	}

	@JsonProperty(value = "grant_type")
	public String getGrantType() {
		return grantType;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((audience == null) ? 0 : audience.hashCode());
		result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result + ((clientSecret == null) ? 0 : clientSecret.hashCode());
		result = prime * result + ((grantType == null) ? 0 : grantType.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OAuthUserAccountTokenRequest other = (OAuthUserAccountTokenRequest) obj;
		if (audience == null) {
			if (other.audience != null)
				return false;
		} else if (!audience.equals(other.audience))
			return false;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		if (clientSecret == null) {
			if (other.clientSecret != null)
				return false;
		} else if (!clientSecret.equals(other.clientSecret))
			return false;
		if (grantType == null) {
			if (other.grantType != null)
				return false;
		} else if (!grantType.equals(other.grantType))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
