package io.zeebe.clustertestbench.cloud.oauth;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Supplier;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.HttpStatus;

public class OAuthInterceptor implements ClientRequestFilter, ClientResponseFilter {
	private static final String HEADER_AUTH_KEY = "Authorization";
	private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
	private static final String GRANT_TYPE_PASWORD = "password";

	private OAuthCredentials credentials;
	private final Supplier<OAuthCredentials> credentialSupplier;

	private OAuthInterceptor(final Supplier<OAuthCredentials> credentialSupplier) {
		this.credentialSupplier = credentialSupplier;
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		synchronized (this) {
			if (credentials == null) {
				credentials = credentialSupplier.get();
			}
		}

		String type = credentials.getTokenType();
		if (type == null || type.isEmpty()) {
			throw new IOException(String.format("Expected valid token type but was absent or invalid '%s'", type));
		}

		type = Character.toUpperCase(type.charAt(0)) + type.substring(1);

		MultivaluedMap<String, Object> headers = requestContext.getHeaders();

		headers.put(HEADER_AUTH_KEY,
				Collections.singletonList(String.format("%s %s", type, credentials.getAccessToken())));
	}

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		if (responseContext.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
			synchronized (this) {
				credentials = null;
			}
		}			
	}

	public static OAuthInterceptor forServiceAccountAuthorization(String authenticationServerURL, String audience,
			String clientId, String clientSecret) {
		final OAuthClient oauthClient = new OAuthClientFactory().createOAuthClient(authenticationServerURL);

		final OAuthServiceAccountTokenRequest tokenRequest = new OAuthServiceAccountTokenRequest(audience, clientId,
				clientSecret, GRANT_TYPE_CLIENT_CREDENTIALS);

		final Supplier<OAuthCredentials> credentialSupplier = () -> oauthClient.requestToken(tokenRequest);

		return new OAuthInterceptor(credentialSupplier);
	}

	public static OAuthInterceptor forUserAccountAuthorization(String authenticationServerURL, String audience,
			String clientId, String clientSecret, String username, String password) {
		final OAuthClient oauthClient = new OAuthClientFactory().createOAuthClient(authenticationServerURL);

		final OAuthUserAccountTokenRequest tokenRequest = new OAuthUserAccountTokenRequest(audience, clientId,
				clientSecret, GRANT_TYPE_PASWORD, username, password);

		final Supplier<OAuthCredentials> credentialSupplier = () -> oauthClient.requestToken(tokenRequest);

		return new OAuthInterceptor(credentialSupplier);
	}

}
