package io.zeebe.clustertestbench.testdriver.impl;

import io.zeebe.client.impl.ZeebeObjectMapper;
import io.zeebe.clustertestbench.testdriver.api.CamundaCloudAuthenticationDetails;

public class CamundaCLoudAuthenticationDetailsImpl implements CamundaCloudAuthenticationDetails {

	private String audience;
	private String authorizationURL;
	private String clientId;
	private String clientSecret;
	private String contactPoint;

	public CamundaCLoudAuthenticationDetailsImpl() {
	}

	public CamundaCLoudAuthenticationDetailsImpl(String authorizationURL, String audience, String contactPoint,
			String clientId, String clientSecret) {
		this.audience = audience;
		this.authorizationURL = authorizationURL;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.contactPoint = contactPoint;
	}

	@Override
	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}

	@Override
	public String getAuthorizationURL() {
		return authorizationURL;
	}

	public void setAuthorizationURL(String authorizationURL) {
		this.authorizationURL = authorizationURL;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	@Override
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
