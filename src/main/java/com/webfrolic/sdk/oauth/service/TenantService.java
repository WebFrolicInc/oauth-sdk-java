package com.webfrolic.sdk.oauth.service;

import com.webfrolic.sdk.oauth.model.Tenant;
import com.webfrolic.sdk.oauth.model.TenantMember;

public class TenantService {

	/**
	 * 
	 * @param tenant
	 * @param accessToken
	 * @param addMember
	 * @return
	 * @throws Exception
	 */
	public static String createTenant(Tenant tenant, String accessToken, boolean addMember) throws Exception {
		String endPoint = "/api/idp/tenant";
		if (addMember) {
			
			endPoint = endPoint + "?member=true&defaultRole=TENANT_ADMINISTRATOR";
		}
		Tenant createdTenant = ApiClient.post(Tenant.class, endPoint, tenant, accessToken);
		if (createdTenant == null) {
			throw new RuntimeException("generic.integration.error");
		}
		String tenantId = createdTenant.getId();

		return tenantId;
	}

	public static String addTenantMember(String tenantId, String customerId, String accessToken) throws Exception {
		TenantMember member = new TenantMember();
		member.setCustomerId(customerId);

		member = ApiClient.post(TenantMember.class, "/api/idp/tenant/" + tenantId + "/member", member, accessToken);
		if (member == null) {
			throw new RuntimeException("generic.integration.error");
		}

		return member.getMemberId();
	}

	public static Tenant getTenant(String tenantId, String accessToken) throws Exception {

		Tenant tenant = ApiClient.query(Tenant.class, "/api/idp/tenant/" + tenantId, accessToken);
		if (tenant == null) {
			throw new RuntimeException("generic.integration.error");
		}

		return tenant;
	}

}
