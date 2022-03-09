package io.zeebe.clustertestbench.cloud.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;

public interface OAuthClient {

  @Consumes("application/json")
  @Produces("application/json")
  @POST
  OAuthCredentials requestToken(ServiceAccountTokenRequest tokenRequest);

  @Consumes("application/json")
  @Produces("application/json")
  @POST
  OAuthCredentials requestToken(UserAccountTokenRequest tokenRequest);

  record ServiceAccountTokenRequest(
      String audience,
      @JsonProperty(value = "client_id") String clientId,
      @JsonProperty(value = "client_secret") String clientSecret,
      @JsonProperty(value = "grant_type") String grantType) {}

  record UserAccountTokenRequest(
      String audience,
      @JsonProperty(value = "client_id") String clientId,
      @JsonProperty(value = "client_secret") String clientSecret,
      @JsonProperty(value = "grant_type") String grantType,
      String username,
      String password) {}
}
