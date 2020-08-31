package io.zeebe.clustertestbench.cloud.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthTokenRequest {

	private final String audience;
	private final String clientId;
	private final String clientSecret;
	private final String grantType;
	
	
	public OAuthTokenRequest(String audience, String clientId, String clientSecret, String grantType) {
		this.audience = audience;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.grantType = grantType;
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
	
}
