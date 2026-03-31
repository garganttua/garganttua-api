package com.garganttua.api.spec.security.authorization;

import com.garganttua.core.reflection.IConstructor;

import com.garganttua.core.reflection.ObjectAddress;

public record AuthorizationInfos(boolean signable, boolean renewable, IConstructor<?> completeConstructor,
		IConstructor<?> rawConstructor, ObjectAddress uuidFieldAddress, ObjectAddress idFieldAddress,
		ObjectAddress tenantIdFieldAddress, ObjectAddress ownerIdFieldAddress,
		ObjectAddress authoritiesFieldAddress, ObjectAddress creationFieldAddress,
		ObjectAddress expirationFieldAddress, ObjectAddress revokedFieldAddress,
		ObjectAddress validateAgainstMethodAddress, ObjectAddress validateMethodAddress,
		ObjectAddress authorizationTypeFieldAddress, ObjectAddress toByteArrayMethodAddress,
		ObjectAddress signMethodAddress, ObjectAddress getRefreshTokenMethodAddress,
		ObjectAddress createRefreshTokenMethodAddress, ObjectAddress validateRefreshTokenMethodAddress,
		ObjectAddress refreshTokenExpirationFielddAddress) {

}
