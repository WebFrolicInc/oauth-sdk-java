package com.webfrolic.sdk.oauth.model;

import java.util.List;

public class ResponsePayLoad<T> {
	
	private Integer nextPageNumber;
	private Integer previousPageNumber;
	
	private List<T> results;
	
	
	
	public Integer getNextPageNumber() {
		return nextPageNumber;
	}
	public void setNextPageNumber(Integer nextPageNumber) {
		this.nextPageNumber = nextPageNumber;
	}
	public Integer getPreviousPageNumber() {
		return previousPageNumber;
	}
	public void setPreviousPageNumber(Integer previousPageNumber) {
		this.previousPageNumber = previousPageNumber;
	}
	public List<T> getResults() {
		return results;
	}
	public void setResults(List<T> results) {
		this.results = results;
	}

}
