package com.webfrolic.sdk.oauth;

import java.io.IOException;

import com.webfrolic.sdk.oauth.model.IdpSession;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface SessionStore {

	public void createUserSession(HttpServletRequest request, HttpServletResponse response, IdpSession idpUser)
			throws IOException;

	public IdpSession getUserSession(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public void saveUserSession(HttpServletRequest request, HttpServletResponse response, IdpSession idpUser)
			throws IOException;

	public void deleteUserSession(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public void addAttribute(HttpServletRequest request, HttpServletResponse response, String attributeName, String attributeValue)
			throws IOException;

	public void removeAttribute(HttpServletRequest request, HttpServletResponse response, String attributeName)
			throws IOException;

	public String getAttribute(HttpServletRequest request, HttpServletResponse response,String attributeName) throws IOException;

	

}
