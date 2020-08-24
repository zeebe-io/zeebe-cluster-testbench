package io.zeebe.clustertestbench.testdriver.api;

import static java.util.Objects.requireNonNull;

public class CamundaCLoudAuthenticationDetails {

	private String audience;
	private String authorizationURL;
	private String clientId;
	private String clientSecret;
	private String contactPoint;

	public CamundaCLoudAuthenticationDetails() {
	}

	public CamundaCLoudAuthenticationDetails(String audience, String authorizationURL, String clientId, String clientSecret,
			String contactPoint) {
		this.audience = requireNonNull(audience);
		this.authorizationURL = requireNonNull(authorizationURL);
		this.clientId = requireNonNull(clientId);
		this.clientSecret = requireNonNull(clientSecret);
		this.contactPoint = requireNonNull(contactPoint);
	}

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

}
