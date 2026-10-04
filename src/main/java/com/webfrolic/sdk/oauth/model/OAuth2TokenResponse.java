package com.webfrolic.sdk.oauth.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Models the successful token endpoint response.
 */
public class OAuth2TokenResponse {

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("id_token")
	private String idToken;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("expires_in")
	private int expiresIn;

	@JsonProperty("scope")
	private String scope;

	

	public String getAccessToken() {
		return accessToken;
	}

	public String getIdToken() {
		return idToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public String getScope() {
		return scope;
	}
}
