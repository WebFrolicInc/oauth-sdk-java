package com.webfrolic.sdk.oauth.exception;

public class SdkException extends RuntimeException {

	private static final long serialVersionUID = 1654595410497498448L;

	public SdkException(String errorCode) {
		super(errorCode);
	}

}
