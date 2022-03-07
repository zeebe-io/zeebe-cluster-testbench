package io.zeebe.clustertestbench.cloud.oauth;

import io.zeebe.clustertestbench.cloud.filter.BadRequestResponseFilter;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class OAuthClientFactory {

  public OAuthClient createOAuthClient(final String authenticationURL) {
    final Client client =
        ClientBuilder.newBuilder().register(BadRequestResponseFilter.class).build();

    final WebTarget target = client.target(authenticationURL);
    final ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
    return rtarget.proxy(OAuthClient.class);
  }
}
