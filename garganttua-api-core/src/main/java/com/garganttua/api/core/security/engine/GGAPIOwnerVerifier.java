package com.garganttua.api.core.security.engine;

import com.garganttua.api.core.security.authentication.GGAPIAuthenticationHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.security.IGGAPIOwnerVerifier;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

public class GGAPIOwnerVerifier implements IGGAPIOwnerVerifier {

	@Override
	public void verifyOwner(IGGAPICaller caller, Object authorization) throws GGAPIException {
		IGGAPIAccessRule rule = caller.getAccessRule();

		if (rule != null && rule.getAccess() == GGAPIServiceAccess.owner ) {
			String authentifiedOwnerId = GGAPIAuthenticationHelper.getOwnerId(authorization);
			String ownerId = caller.getOwnerId();

			if (!authentifiedOwnerId.equals(ownerId) && !caller.isSuperOwner()) {
				throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Requested OwnerId [" + ownerId + "] and authentifed user's OwnerId ["
						+ authentifiedOwnerId + "] do not match");
			}
		}
	}
}
