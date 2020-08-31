package io.zeebe.clustertestbench.cloud.oauth;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


public interface OAuthClient {

	
	@Consumes("application/json")
	@Produces("application/json")
	@POST
	public OAuthCredentials requestToken(OAuthTokenRequest tokenRequest);
}
