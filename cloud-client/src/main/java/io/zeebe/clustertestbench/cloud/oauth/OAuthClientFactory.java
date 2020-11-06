package io.zeebe.clustertestbench.cloud.oauth;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class OAuthClientFactory {

  public OAuthClient createOAuthClient(String authenticationURL) {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(authenticationURL);
    ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
    return rtarget.proxy(OAuthClient.class);
  }
}
