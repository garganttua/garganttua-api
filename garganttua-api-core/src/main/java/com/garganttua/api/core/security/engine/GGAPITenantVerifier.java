package com.garganttua.api.core.security.engine;

import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.security.IGGAPITenantVerifier;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

public class GGAPITenantVerifier implements IGGAPITenantVerifier {

	@Override
	public void verifyTenant(IGGAPICaller caller, Object authorization) throws GGAPIException {
		
		IGGAPIAccessRule rule = caller.getAccessRule();

		if ( rule != null && (rule.getAccess() == GGAPIServiceAccess.tenant || rule.getAccess() == GGAPIServiceAccess.owner) ) {
			String authentifiedTenantId = GGAPIEntityAuthorizationHelper.getTenantId(authorization);;
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
