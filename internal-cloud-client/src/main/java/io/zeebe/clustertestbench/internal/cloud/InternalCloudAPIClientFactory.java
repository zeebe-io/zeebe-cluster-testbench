package io.zeebe.clustertestbench.internal.cloud;

import io.zeebe.clustertestbench.cloud.oauth.OAuthInterceptor;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class InternalCloudAPIClientFactory {

  public InternalCloudAPIClient createCloudAPIClient(
      String cloudApiUrl,
      String authenticationServerURL,
      String audience,
      String clientId,
      String clientSecret,
      String username,
      String password) {

    OAuthInterceptor oauthInterceptor =
        OAuthInterceptor.forUserAccountAuthorization(
            authenticationServerURL, audience, clientId, clientSecret, username, password);

    Client client = ClientBuilder.newBuilder().register(oauthInterceptor).build();
    WebTarget target = client.target(cloudApiUrl);
    ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
    return rtarget.proxy(InternalCloudAPIClient.class);
  }
}
