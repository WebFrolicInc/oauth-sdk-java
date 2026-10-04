package com.webfrolic.sdk.oauth.model;

import java.util.Map;

public class Tenant {

	private String id = null;
	private String name = null;
	private String status = null;
	private String description = null;
	private String homePage = null;
	private Map<String, String> attributes = null;

	public Tenant() {

	}

	public Tenant(String tenantName) {

		this.name = tenantName;
	}

	public Tenant(String tenantName, String description, String homePage) {

		this.name = tenantName;
		this.description = description;
		this.homePage = homePage;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getHomePage() {
		return homePage;
	}

	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
}
