package io.zeebe.clustertestbench.internal.cloud;

import io.zeebe.clustertestbench.cloud.filter.EntityLoggingFilter;
import io.zeebe.clustertestbench.cloud.filter.FailedResponseFilter;
import io.zeebe.clustertestbench.cloud.oauth.OAuthInterceptor;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class ExternalConsoleAPIClientFactory {

  public ExternalConsoleAPIClient createConsoleAPIClient(
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
            .register(EntityLoggingFilter.class)
            .register(FailedResponseFilter.class)
            .build();
    final WebTarget target = client.target(cloudApiUrl);
    final ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
    return rtarget.proxy(ExternalConsoleAPIClient.class);
  }
}
