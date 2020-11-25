package io.zeebe.clustertestbench.cloud;

import io.zeebe.clustertestbench.cloud.filter.BadRequestResponseFilter;
import io.zeebe.clustertestbench.cloud.oauth.OAuthInterceptor;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class CloudAPIClientFactory {

  public CloudAPIClient createCloudAPIClient(
      String cloudApiUrl,
      String authenticationServerURL,
      String audience,
      String clientId,
      String clientSecret) {

    OAuthInterceptor oauthInterceptor =
        OAuthInterceptor.forServiceAccountAuthorization(
            authenticationServerURL, audience, clientId, clientSecret);

    Client client =
        ClientBuilder.newBuilder()
            .register(oauthInterceptor)
            .register(BadRequestResponseFilter.class)
            .build();
    WebTarget target = client.target(cloudApiUrl);
    ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
    return rtarget.proxy(CloudAPIClient.class);
  }
}
