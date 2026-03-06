package com.garganttua.api.spec.security.authentication;

import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public record AuthenticationInfos(
		IClass<?> authenticationType,
		ObjectAddress autoritiesFieldAddress,
		ObjectAddress authenticatorServiceFieldAddress,
		ObjectAddress authorizationFieldAddress,
		ObjectAddress authenticatedFieldAddress,
		ObjectAddress principalFieldAddress,
		ObjectAddress credentialsFieldAddress,
		ObjectAddress tenantIdFieldAddress,
		ObjectAddress authenticateMethodAddress,
		ObjectAddress authenticatorInfosFieldAddress,
		boolean findPrincipal,
		ObjectAddress findPrincipalMethodAddress,
		ObjectAddress ownerIdFieldAddress,
		ObjectAddress securityPreProcessingMethodAddress,
		ObjectAddress securityPostProcessingMethodAddress
		) {

}
