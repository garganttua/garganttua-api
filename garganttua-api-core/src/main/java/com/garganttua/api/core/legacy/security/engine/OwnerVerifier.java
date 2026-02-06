package com.garganttua.api.core.legacy.security.engine;

import com.garganttua.api.core.legacy.security.authentication.AuthenticationHelper;
import com.garganttua.api.core.legacy.security.exceptions.SecurityException;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.security.IOwnerVerifier;
import com.garganttua.api.spec.service.ServiceAccess;

public class OwnerVerifier implements IOwnerVerifier {

	@Override
	public void verifyOwner(ICaller caller, Object authorization) throws CoreException {

		if ( caller.getAccess() == ServiceAccess.owner ) {
			String authentifiedOwnerId = AuthenticationHelper.getOwnerId(authorization);
			String ownerId = caller.getOwnerId();

			if (!authentifiedOwnerId.equals(ownerId) && !caller.isSuperOwner()) {
				throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Requested OwnerId [" + ownerId + "] and authentifed user's OwnerId ["
						+ authentifiedOwnerId + "] do not match");
			}
		}
	}
}
