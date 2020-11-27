package io.zeebe.clustertestbench.testdriver.impl;

import io.zeebe.client.impl.ZeebeObjectMapper;
import io.zeebe.clustertestbench.testdriver.api.CamundaCloudAuthenticationDetails;

public class CamundaCLoudAuthenticationDetailsImpl implements CamundaCloudAuthenticationDetails {

  private String audience;
  private String authorizationURL;
  private String clientId;
  private String clientSecret;
  private String contactPoint;

  public CamundaCLoudAuthenticationDetailsImpl() {}

  public CamundaCLoudAuthenticationDetailsImpl(
      final String authorizationURL,
      final String audience,
      final String contactPoint,
      final String clientId,
      final String clientSecret) {
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

  public void setAudience(final String audience) {
    this.audience = audience;
  }

  @Override
  public String getAuthorizationURL() {
    return authorizationURL;
  }

  public void setAuthorizationURL(final String authorizationURL) {
    this.authorizationURL = authorizationURL;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  public void setClientId(final String clientId) {
    this.clientId = clientId;
  }

  @Override
  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(final String clientSecret) {
    this.clientSecret = clientSecret;
  }

  @Override
  public String getContactPoint() {
    return contactPoint;
  }

  public void setContactPoint(final String contactPoint) {
    this.contactPoint = contactPoint;
  }

  @Override
  public String toString() {
    return new ZeebeObjectMapper().toJson(this);
  }
}
