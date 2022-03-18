package io.zeebe.clustertestbench.cloud.oauth;

import io.zeebe.clustertestbench.cloud.oauth.OAuthClient.OAuthCredentials;
import io.zeebe.clustertestbench.cloud.oauth.OAuthClient.ServiceAccountTokenRequest;
import io.zeebe.clustertestbench.cloud.oauth.OAuthClient.UserAccountTokenRequest;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Collections;
import java.util.function.Supplier;
import org.apache.http.HttpStatus;

public final class OAuthInterceptor implements ClientRequestFilter, ClientResponseFilter {
  private static final String HEADER_AUTH_KEY = "Authorization";
  private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
  private static final String GRANT_TYPE_PASSWORD = "password";

  private OAuthCredentials credentials;
  private final Supplier<OAuthCredentials> credentialSupplier;

  private OAuthInterceptor(final Supplier<OAuthCredentials> credentialSupplier) {
    this.credentialSupplier = credentialSupplier;
  }

  @Override
  public void filter(final ClientRequestContext requestContext) throws IOException {
    synchronized (this) {
      if (credentials == null) {
        credentials = credentialSupplier.get();
      }
    }

    String type = credentials.tokenType();
    if (type == null || type.isEmpty()) {
      throw new IOException(
          String.format("Expected valid token type but was absent or invalid '%s'", type));
    }

    type = Character.toUpperCase(type.charAt(0)) + type.substring(1);

    final MultivaluedMap<String, Object> headers = requestContext.getHeaders();

    headers.put(
        HEADER_AUTH_KEY,
        Collections.singletonList(String.format("%s %s", type, credentials.accessToken())));
  }

  @Override
  public void filter(
      final ClientRequestContext requestContext, final ClientResponseContext responseContext)
      throws IOException {
    if (responseContext.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
      synchronized (this) {
        credentials = null;
      }
    }
  }

  public static OAuthInterceptor forServiceAccountAuthorization(
      final String authenticationServerURL,
      final String audience,
      final String clientId,
      final String clientSecret) {
    final OAuthClient oauthClient =
        new OAuthClientFactory().createOAuthClient(authenticationServerURL);

    final ServiceAccountTokenRequest tokenRequest =
        new ServiceAccountTokenRequest(
            audience, clientId, clientSecret, GRANT_TYPE_CLIENT_CREDENTIALS);

    final Supplier<OAuthCredentials> credentialSupplier =
        () -> oauthClient.requestToken(tokenRequest);

    return new OAuthInterceptor(credentialSupplier);
  }

  public static OAuthInterceptor forUserAccountAuthorization(
      final String authenticationServerURL,
      final String audience,
      final String clientId,
      final String clientSecret,
      final String username,
      final String password) {
    final OAuthClient oauthClient =
        new OAuthClientFactory().createOAuthClient(authenticationServerURL);

    final UserAccountTokenRequest tokenRequest =
        new UserAccountTokenRequest(
            audience, clientId, clientSecret, GRANT_TYPE_PASSWORD, username, password);

    final Supplier<OAuthCredentials> credentialSupplier =
        () -> oauthClient.requestToken(tokenRequest);

    return new OAuthInterceptor(credentialSupplier);
  }
}
