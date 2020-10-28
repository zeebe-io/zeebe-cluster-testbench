package io.zeebe.clustertestbench.cloud.oauth;

import java.util.Optional;

public interface OAuthCredentialsCache {

	Optional<OAuthCredentials> get();

	void put(final OAuthCredentials credentials);

	void remove();
}
