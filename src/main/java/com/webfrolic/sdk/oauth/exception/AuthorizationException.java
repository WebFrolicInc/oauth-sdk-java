package com.webfrolic.sdk.oauth.exception;

public class AuthorizationException extends RuntimeException{

	private static final long serialVersionUID = 1654595410497498448L;
	
	public AuthorizationException() {
		super("authorization failure");
	}

}
