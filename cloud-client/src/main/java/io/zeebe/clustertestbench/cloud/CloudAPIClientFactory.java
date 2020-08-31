package io.zeebe.clustertestbench.cloud;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import io.zeebe.clustertestbench.cloud.oauth.OAuthInterceptor;

public class CloudAPIClientFactory {

	public CloudAPIClient createCloudAPIClient(String cloudApiUrl, String authenticationServerURL, String audience,
			String clientId, String clientSecret) {

		OAuthInterceptor oauthInterceptor = new OAuthInterceptor(authenticationServerURL, audience, clientId,
				clientSecret);

		Client client = ClientBuilder.newBuilder().register(oauthInterceptor).build();
		WebTarget target = client.target(cloudApiUrl);
		ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
		return rtarget.proxy(CloudAPIClient.class);
	}

}
