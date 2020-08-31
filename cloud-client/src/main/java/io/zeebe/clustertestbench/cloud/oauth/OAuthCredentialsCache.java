package io.zeebe.clustertestbench.cloud.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class OAuthCredentialsCache {

	private static final Map<String, OAuthCredentialsCache> serverToCacheMap = new HashMap<>();

	private final Map<String, OAuthCredentials> audienceToCredentialMap = new HashMap<>();

	// kept for debugging
	private final String authenticationServerURL;

	public OAuthCredentialsCache(String authenticationServerURL) {
		this.authenticationServerURL = authenticationServerURL;
	}

	public synchronized Optional<OAuthCredentials> get(final String audience) {
		return Optional.ofNullable(audienceToCredentialMap.get(audience)).filter(credentials -> credentials.isValid());
	}

	public synchronized void put(final String audience, final OAuthCredentials credentials) {
		audienceToCredentialMap.put(audience, credentials);
	}

	public synchronized void remove(final String audeince) {
		audienceToCredentialMap.remove(audeince);
	}

	static synchronized OAuthCredentialsCache getInstance(String authenticationServerURL) {
		return serverToCacheMap.computeIfAbsent(authenticationServerURL, OAuthCredentialsCache::new);
	}

	@Override
	public String toString() {
		return "OAuthCredentialsCache [authenticationServerURL=" + authenticationServerURL
				+ ", audienceToCredentialMap=\"" + audienceToCredentialMap + "]";
	}

}
