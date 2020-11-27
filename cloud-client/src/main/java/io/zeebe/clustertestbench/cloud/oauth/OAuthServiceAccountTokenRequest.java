package io.zeebe.clustertestbench.cloud.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthServiceAccountTokenRequest {

  private final String audience;
  private final String clientId;
  private final String clientSecret;
  private final String grantType;

  public OAuthServiceAccountTokenRequest(
      final String audience,
      final String clientId,
      final String clientSecret,
      final String grantType) {
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((audience == null) ? 0 : audience.hashCode());
    result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
    result = prime * result + ((clientSecret == null) ? 0 : clientSecret.hashCode());
    result = prime * result + ((grantType == null) ? 0 : grantType.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final OAuthServiceAccountTokenRequest other = (OAuthServiceAccountTokenRequest) obj;
    if (audience == null) {
      if (other.audience != null) {
        return false;
      }
    } else if (!audience.equals(other.audience)) {
      return false;
    }
    if (clientId == null) {
      if (other.clientId != null) {
        return false;
      }
    } else if (!clientId.equals(other.clientId)) {
      return false;
    }
    if (clientSecret == null) {
      if (other.clientSecret != null) {
        return false;
      }
    } else if (!clientSecret.equals(other.clientSecret)) {
      return false;
    }
    if (grantType == null) {
      if (other.grantType != null) {
        return false;
      }
    } else if (!grantType.equals(other.grantType)) {
      return false;
    }
    return true;
  }
}
