package com.webfrolic.sdk.oauth.model;

public class IdpUser {

	private String subjectId = null;
	private String firstName = null;
	private String lastName = null;
	private String email = null;
	private String role = null;
	private String tenantId = null;
	private String tenantRole = null;

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getTenantRole() {
		return tenantRole;
	}

	public void setTenantRole(String tenantRole) {
		this.tenantRole = tenantRole;
	}

}
