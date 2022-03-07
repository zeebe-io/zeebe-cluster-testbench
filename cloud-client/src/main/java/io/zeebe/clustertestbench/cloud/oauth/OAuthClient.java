package io.zeebe.clustertestbench.cloud.oauth;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;

public interface OAuthClient {

  @Consumes("application/json")
  @Produces("application/json")
  @POST
  public OAuthCredentials requestToken(OAuthServiceAccountTokenRequest tokenRequest);

  @Consumes("application/json")
  @Produces("application/json")
  @POST
  public OAuthCredentials requestToken(OAuthUserAccountTokenRequest tokenRequest);
}
