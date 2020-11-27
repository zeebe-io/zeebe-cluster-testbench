package io.zeebe.clustertestbench.bootstrap;

public class OAuthServiceAccountAuthenticationDetails {

  private final String serverURL;
  private final String audience;
  private final String clientId;
  private final String clientSecret;

  public OAuthServiceAccountAuthenticationDetails(
      final String serverURL,
      final String audience,
      final String clientId,
      final String clientSecret) {
    this.serverURL = serverURL;
    this.audience = audience;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  public String getServerURL() {
    return serverURL;
  }

  public String getAudience() {
    return audience;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  @Override
  public String toString() {
    return "OAuthAuthenticationDetails [serverURL="
        + serverURL
        + ", audience="
        + audience
        + ", clientId="
        + clientId
        + ", clientSecret="
        + clientSecret
        + "]";
  }
}
