package com.webfrolic.sdk.oauth;

import java.io.IOException;

import com.webfrolic.sdk.oauth.model.IdpSession;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DefaultSessionStoreImpl implements SessionStore {

	private static final String IDP_USER = "IDP_USER";
	
	@Override
	public void createUserSession(HttpServletRequest request, HttpServletResponse response, IdpSession idpUser)
			throws IOException {
		request.getSession().setAttribute(IDP_USER, idpUser);

	}

	@Override
	public IdpSession getUserSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return (IdpSession) request.getSession().getAttribute(IDP_USER);
	}

	@Override
	public void saveUserSession(HttpServletRequest request, HttpServletResponse response, IdpSession idpUser)
			throws IOException {
		request.getSession().setAttribute(IDP_USER, idpUser);

	}

	@Override
	public void deleteUserSession(HttpServletRequest request, HttpServletResponse response) throws IOException {

		request.getSession().removeAttribute(IDP_USER);
		request.getSession().invalidate();

	}

	@Override
	public void addAttribute(HttpServletRequest request, HttpServletResponse response, String attributeName,
			String attributeValue) throws IOException {
		request.getSession().setAttribute(attributeName, attributeValue);

	}

	@Override
	public void removeAttribute(HttpServletRequest request, HttpServletResponse response, String attributeName)
			throws IOException {
		request.getSession().removeAttribute(attributeName);

	}

	@Override
	public String getAttribute(HttpServletRequest request, HttpServletResponse response, String attributeName)
			throws IOException {
		return (String) request.getSession().getAttribute(attributeName);
	}

}
