package io.zeebe.clustertestbench.cloud.oauth;

import io.zeebe.clustertestbench.cloud.filter.BadRequestResponseFilter;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class OAuthClientFactory {

  public OAuthClient createOAuthClient(String authenticationURL) {
    Client client = ClientBuilder.newBuilder().register(BadRequestResponseFilter.class).build();

    WebTarget target = client.target(authenticationURL);
    ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
    return rtarget.proxy(OAuthClient.class);
  }
}
