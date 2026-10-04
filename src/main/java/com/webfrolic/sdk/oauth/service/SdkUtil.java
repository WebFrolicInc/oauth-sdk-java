package com.webfrolic.sdk.oauth.service;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webfrolic.sdk.oauth.exception.AuthorizationException;
import com.webfrolic.sdk.oauth.model.IdpSession;
import com.webfrolic.sdk.oauth.model.PairBean;

import jakarta.servlet.http.HttpServletRequest;

public class SdkUtil {

	private static final Logger logger = LoggerFactory.getLogger(SdkUtil.class);

	protected static String idpServer = null;
	
	protected static String resourceAccessToken = null;

	protected static String tenantAccessToken = null;

	public static String getTenantAdminConsoleEndpoint() {
		return idpServer+"/account/admin/home";
	}
	
	public static void initWithResourceToken(String accessToken) {
		resourceAccessToken = accessToken;
	}
	
	public static void initServerEndPoint(String server) {
		idpServer = server;
	}

	public static void initWithTenantToken(String accessToken) {
		tenantAccessToken = accessToken;
	}

	public static PairBean<String> getIpAddressFromRequest(HttpServletRequest request) {
		return getIpAddressFromRequest(request, true);

	}

	public static String getTenantAccessToken() {
		if (tenantAccessToken != null) {
			return tenantAccessToken;
		}
		throw new AuthorizationException();
	}

	
	public static String getResourceAccessToken() {
		if (resourceAccessToken != null) {
			return resourceAccessToken;
		}
		throw new AuthorizationException();
	}

	
	public String getIpAddress(IdpSession idpSession) {
		if (idpSession.getClientIpAddress() != null) {
			if (idpSession.getClientIpAddress().equals(idpSession.getProxyIpAddress())) {
				return idpSession.getClientIpAddress();
			} else {
				return idpSession.getProxyIpAddress();
			}
		}
		return null;
	}
	
	
	public static PairBean<String> getIpAddressFromRequest(HttpServletRequest request, boolean checkTenantProxy) {

		String ipAddress = request.getHeader("X-Forwarded-For");
		if (ipAddress == null || ipAddress.isEmpty()) {
			ipAddress = request.getHeader("X-Real-IP");
		}
		if (ipAddress == null || ipAddress.isEmpty()) {
			ipAddress = request.getHeader("wl-proxy-client-ip");
		}
		if (ipAddress == null || ipAddress.isEmpty()) {
			ipAddress = request.getRemoteAddr();
		}
		if (ipAddress != null) {			
			String[] ips = ipAddress.split(",");	
			if (ips.length == 1) {
				return new PairBean<String>(ips[0].trim(), ips[0].trim());
			}

			Set<String> tenantTrustedProxies = new HashSet<>();

			if (checkTenantProxy) {
				
				
				
				if (ips.length < 3) {
					logger.warn(
							"Error with settings for proxy setup, check it again");

					return new PairBean<String>(ips[0].trim(), ips[0].trim());
				} else {

					int index = ips.length - 1;
					for (int i = 0; i < 2; i++) {
						tenantTrustedProxies.add(ips[index].trim());
						index--;
					}

				}
			} else {
				
				return new PairBean<String>(ips[0].trim(), ips[0].trim());
			}
			
			for (int i = 1; i < ips.length; i++) {			
				if (tenantTrustedProxies.contains(ips[i].trim())) {
					return new PairBean<String>(ips[0].trim(), ips[i - 1].trim());
				}
			}
			ipAddress = ips[0].trim();
		}

		return new PairBean<String>(ipAddress, ipAddress);
	}

}
