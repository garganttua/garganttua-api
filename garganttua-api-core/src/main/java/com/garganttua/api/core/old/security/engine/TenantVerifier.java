package com.garganttua.api.core.security.engine;

import com.garganttua.api.core.security.authentication.AuthenticationHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.security.ITenantVerifier;
import com.garganttua.api.spec.service.ServiceAccess;

public class TenantVerifier implements ITenantVerifier {

	@Override
	public void verifyTenant(ICaller caller, Object authorization) throws CoreException {

		if ( caller.getAccess() == ServiceAccess.tenant || caller.getAccess() == ServiceAccess.owner ) {
			String authentifiedTenantId = AuthenticationHelper.getTenantId(authorization);
			String tenantId = caller.getTenantId();
			String requestedTenantId = caller.getRequestedTenantId();

			if ( !authentifiedTenantId.equals(tenantId) ) {
				throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "TenantId [" + tenantId + "] and authentifed user's tenantId ["
						+ authentifiedTenantId + "] do not match");
			}
			
			if( !caller.isSuperTenant() && !requestedTenantId.equals(authentifiedTenantId) ) {
				throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authentifed user's tenant ["
						+ authentifiedTenantId + "] is not super tenant and cannot access to other tenant");
			}
		}
	}
}
