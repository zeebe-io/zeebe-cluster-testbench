package io.zeebe.clustertestbench.cloud.oauth;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.HttpStatus;

public class OAuthInterceptor implements ClientRequestFilter, ClientResponseFilter {
	private static final String HEADER_AUTH_KEY = "Authorization";
	private static final String GRANT_TYPE = "client_credentials";

	private final OAuthCredentialsCache credentialsCache;
	private final String audience;
	private final OAuthClient oauthClient;
	private final OAuthTokenRequest tokenRequest;

	public OAuthInterceptor(String authenticationServerURL, String audience, String clientId, String clientSecret) {
		this.credentialsCache = OAuthCredentialsCache.getInstance(authenticationServerURL);
		this.audience = audience;

		this.oauthClient = new OAuthClientFactory().createOAuthClient(authenticationServerURL);

		tokenRequest = new OAuthTokenRequest(audience, clientId, clientSecret, GRANT_TYPE);
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		Optional<OAuthCredentials> optCredentials = credentialsCache.get(audience);

		final OAuthCredentials credentials;
		if (optCredentials.isEmpty()) {
			credentials = oauthClient.requestToken(tokenRequest);
			credentialsCache.put(audience, credentials);
		} else {
			credentials = optCredentials.get();
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
			credentialsCache.remove(audience);
		}
	}

}
