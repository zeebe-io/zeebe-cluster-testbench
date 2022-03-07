package io.zeebe.clustertestbench.internal.cloud;

import io.zeebe.clustertestbench.cloud.filter.BadRequestResponseFilter;
import io.zeebe.clustertestbench.cloud.oauth.OAuthInterceptor;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class InternalCloudAPIClientFactory {

  public InternalCloudAPIClient createCloudAPIClient(
      final String cloudApiUrl,
      final String authenticationServerURL,
      final String audience,
      final String clientId,
      final String clientSecret,
      final String username,
      final String password) {

    final OAuthInterceptor oauthInterceptor =
        OAuthInterceptor.forUserAccountAuthorization(
            authenticationServerURL, audience, clientId, clientSecret, username, password);

    final Client client =
        ClientBuilder.newBuilder()
            .register(oauthInterceptor)
            .register(BadRequestResponseFilter.class)
            .build();
    final WebTarget target = client.target(cloudApiUrl);
    final ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
    return rtarget.proxy(InternalCloudAPIClient.class);
  }
}
