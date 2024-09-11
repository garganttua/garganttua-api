package com.garganttua.api.security.core.engine;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPITenantVerifier;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

public class GGAPITenantVerifier implements IGGAPITenantVerifier {

	@Override
	public void verifyTenant(IGGAPICaller caller, IGGAPIAuthorization authorization) throws GGAPIException {
		
		IGGAPIAccessRule rule = caller.getAccessRule();

		if ( rule != null && rule.getAccess() == GGAPIServiceAccess.tenant ) {
			String authentifiedTenantId = authorization.getTenantId();
			String tenantId = caller.getTenantId();
			String requestedTenantId = caller.getRequestedTenantId();

			if ( !authentifiedTenantId.equals(tenantId) ) {
				throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "TenantId [" + tenantId + "] and authentifed user's tenantId ["
						+ authentifiedTenantId + "] do not match");
			}
			
			if( !caller.isSuperTenant() && !requestedTenantId.equals(authentifiedTenantId) ) {
				throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Authentifed user's tenant ["
						+ authentifiedTenantId + "] is not super tenant and cannot access to other tenant");
			}
		}
	}
}