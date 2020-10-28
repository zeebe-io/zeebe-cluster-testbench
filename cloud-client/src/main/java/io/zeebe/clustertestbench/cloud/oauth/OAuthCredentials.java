package io.zeebe.clustertestbench.cloud.oauth;

import java.time.Instant;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthCredentials {

	@JsonAlias({ "accesstoken", "access_token" })
	private String accessToken;

	private ZonedDateTime expiry;

	@JsonAlias({ "tokentype", "token_type" })
	private String tokenType;

	public OAuthCredentials() {
	}

	public OAuthCredentials(final String accessToken, final ZonedDateTime expiry, final String tokenType) {
		this.accessToken = accessToken;
		this.expiry = expiry;
		this.tokenType = tokenType;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public ZonedDateTime getExpiry() {
		return expiry;
	}

	@JsonSetter("expiry")
	public void setExpiry(final String expiry) {
		this.expiry = ZonedDateTime.parse(expiry);
	}

	@JsonSetter("expires_in")
	public void setExpiresIn(final String expiresIn) {
		this.expiry = ZonedDateTime.now().plusSeconds(Long.parseLong(expiresIn));
	}

	@JsonIgnore
	public boolean isValid() {
		return expiry.toInstant().isAfter(Instant.now());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
		result = prime * result + ((expiry == null) ? 0 : expiry.hashCode());
		result = prime * result + ((tokenType == null) ? 0 : tokenType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OAuthCredentials other = (OAuthCredentials) obj;
		if (accessToken == null) {
			if (other.accessToken != null)
				return false;
		} else if (!accessToken.equals(other.accessToken))
			return false;
		if (expiry == null) {
			if (other.expiry != null)
				return false;
		} else if (!expiry.equals(other.expiry))
			return false;
		if (tokenType == null) {
			if (other.tokenType != null)
				return false;
		} else if (!tokenType.equals(other.tokenType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OAuthCredentials [accessToken=" + accessToken + ", expiry=" + expiry + ", tokenType=" + tokenType + "]";
	}

}