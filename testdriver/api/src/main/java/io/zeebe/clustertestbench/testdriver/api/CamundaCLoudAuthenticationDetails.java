package io.zeebe.clustertestbench.testdriver.api;

import io.zeebe.client.impl.ZeebeObjectMapper;

public class CamundaCLoudAuthenticationDetails {
	
	public static final String VARIABLE_KEY = "authenticationDetails";

	private String audience;
	private String authorizationURL;
	private String clientId;
	private String clientSecret;
	private String contactPoint;

	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}

	public String getAuthorizationURL() {
		return authorizationURL;
	}

	public void setAuthorizationURL(String authorizationURL) {
		this.authorizationURL = authorizationURL;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getContactPoint() {
		return contactPoint;
	}

	public void setContactPoint(String contactPoint) {
		this.contactPoint = contactPoint;
	}
	
	@Override
	public String toString() {
		return new ZeebeObjectMapper().toJson(this);
	}

}
