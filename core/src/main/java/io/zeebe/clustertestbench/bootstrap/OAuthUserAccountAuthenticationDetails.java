package io.zeebe.clustertestbench.bootstrap;

public class OAuthUserAccountAuthenticationDetails {

  private final String serverURL;
  private final String audience;
  private final String clientId;
  private final String clientSecret;
  private final String username;
  private final String password;

  public OAuthUserAccountAuthenticationDetails(
      final String serverURL,
      final String audience,
      final String clientId,
      final String clientSecret,
      final String username,
      final String password) {
    this.serverURL = serverURL;
    this.audience = audience;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.username = username;
    this.password = password;
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

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public String toString() {
    return "OAuthUserAccountAuthenticationDetails [serverURL="
        + serverURL
        + ", audience="
        + audience
        + ", clientId="
        + clientId
        + ", clientSecret="
        + clientSecret
        + ", username="
        + username
        + ", password="
        + password
        + "]";
  }
}
