package com.garganttua.api.spec.security.authentication;

import com.garganttua.reflection.GGObjectAddress;

public record GGAPIAuthenticationInfos(
		Class<?> authenticationType,
		GGObjectAddress autoritiesFieldAddress,
		GGObjectAddress authenticatorServiceFieldAddress,
		GGObjectAddress authorizationFieldAddress,
		GGObjectAddress authenticatedFieldAddress,
		GGObjectAddress principalFieldAddress,
		GGObjectAddress credentialsFieldAddress,
		GGObjectAddress tenantIdFieldAddress,
		GGObjectAddress authenticateMethodAddress,
		GGObjectAddress authenticatorInfosFieldAddress,
		boolean findPrincipal
		) {

}
