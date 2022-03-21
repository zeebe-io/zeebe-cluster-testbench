package io.zeebe.clustertestbench.testdriver.api;

import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

public record CamundaCloudAuthenticationDetails(
    String audience,
    String authorizationURL,
    String clientId,
    String clientSecret,
    String contactPoint) {

  public static final String VARIABLE_KEY = "authenticationDetails";

  public OAuthCredentialsProvider buildCredentialsProvider() {
    if (authorizationURL == null) {
      return new OAuthCredentialsProviderBuilder()
          .audience(audience)
          .clientId(clientId)
          .clientSecret(clientSecret)
          .build();
    } else {
      return new OAuthCredentialsProviderBuilder()
          .authorizationServerUrl(authorizationURL)
          .audience(audience)
          .clientId(clientId)
          .clientSecret(clientSecret)
          .build();
    }
  }
}
