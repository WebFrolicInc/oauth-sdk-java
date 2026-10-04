package com.webfrolic.sdk.oauth.model;

public class IdpSession extends IdpUser {
	private String accessToken = null;
	private String sessionId = null;
	private long lastUpdated = 0;
	private String session = null;
	private String clientIpAddress;
	private String proxyIpAddress;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public String getClientIpAddress() {
		return clientIpAddress;
	}

	public void setClientIpAddress(String clientIpAddress) {
		this.clientIpAddress = clientIpAddress;
	}

	public String getProxyIpAddress() {
		return proxyIpAddress;
	}

	public void setProxyIpAddress(String proxyIpAddress) {
		this.proxyIpAddress = proxyIpAddress;
	}
}
