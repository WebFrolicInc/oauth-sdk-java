package com.webfrolic.sdk.oauth.model;

import java.util.List;

public class QueryResponse<T> {

	
	private String nextToken = null;
	private List<T> result;

	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}

	public String getNextToken() {
		return nextToken;
	}

	public void setNextToken(String nextToken) {
		this.nextToken = nextToken;
	}
}
