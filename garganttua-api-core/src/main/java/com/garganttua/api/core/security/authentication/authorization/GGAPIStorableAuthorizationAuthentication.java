package com.garganttua.api.core.security.authentication.authorization;

import java.util.HashMap;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GGAPIAuthentication(findPrincipal = true)
@NoArgsConstructor
public class GGAPIStorableAuthorizationAuthentication extends AbstractGGAPIAuthentication {

	@Override
	protected void doAuthentication() throws GGAPIException {
		if( GGAPIEntityAuthorizationHelper.isAuthorization(this.principal) ) {
			GGAPIEntityAuthorizationHelper.validateAgainst(this.credential, this.principal);
			this.ownerId = GGAPIEntityAuthorizationHelper.getOwnerId(this.principal);
			this.authorities = GGAPIEntityAuthorizationHelper.getAuthorities(this.principal);
		} else {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Principal is not an authorization");
		}
		this.authenticated = true;
	}
	
	@Override
	protected Object doFindPrincipal() {
		String ownerId;
		try {
			ownerId = GGAPIEntityAuthorizationHelper.getOwnerId(this.credential);
			String uuid = GGAPIEntityAuthorizationHelper.getUuid(this.credential);
			IGGAPIServiceResponse response = this.authenticatorService.getEntity(GGAPICaller.createTenantCallerWithOwnerId(this.tenantId, ownerId), uuid, new HashMap<String, String>());
	
			if( response.getResponseCode() == GGAPIServiceResponseCode.OK ) {
				log.atDebug().log("Found principal identified with uuid "+uuid);
				return response.getResponse();
			} else {
				log.atDebug().log("Failed to find principal identified with uuid "+uuid);
				return null;
			}
		} catch (GGAPIException e) {
			log.atDebug().log("Failed to find principal", e);
			return null;
		}
	}
}
