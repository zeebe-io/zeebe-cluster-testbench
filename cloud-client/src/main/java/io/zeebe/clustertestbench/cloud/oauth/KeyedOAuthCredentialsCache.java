package io.zeebe.clustertestbench.cloud.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class KeyedOAuthCredentialsCache {

	private static final Map<Object, OAuthCredentials> cachedCredentials = new HashMap<>();

	private static synchronized Optional<OAuthCredentials> get(final Object key) {
		return Optional.ofNullable(cachedCredentials.get(key)).filter(credentials -> credentials.isValid());
	}

	private static synchronized void put(final Object key, final OAuthCredentials credentials) {
		cachedCredentials.put(key, credentials);
	}

	private static synchronized void remove(final Object key) {
		cachedCredentials.remove(key);
	}

	public static synchronized OAuthCredentialsCache getCredentialsCache(Object request) {
		return new OAuthCredentialsCacheImpl(request);
	}

	protected static class OAuthCredentialsCacheImpl implements OAuthCredentialsCache {

		private final Object key;

		private OAuthCredentialsCacheImpl(Object key) {
			this.key = key;
		}

		@Override
		public Optional<OAuthCredentials> get() {
			return KeyedOAuthCredentialsCache.get(key);
		}

		@Override
		public void put(OAuthCredentials credentials) {
			KeyedOAuthCredentialsCache.put(key, credentials);
		}

		@Override
		public void remove() {
			KeyedOAuthCredentialsCache.remove(key);
		}
	}
}
