package com.garganttua.api.spec.security.authorization;

import java.lang.reflect.Constructor;

import com.garganttua.reflection.GGObjectAddress;

public record GGAPIAuthorizationInfos(boolean signable, boolean renewable, Constructor<?> completeConstructor,
		Constructor<?> rawConstructor, GGObjectAddress uuidFieldAddress, GGObjectAddress idFieldAddress,
		GGObjectAddress tenantIdFieldAddress, GGObjectAddress ownerIdFieldAddress,
		GGObjectAddress authoritiesFieldAddress, GGObjectAddress creationFieldAddress,
		GGObjectAddress expirationFieldAddress, GGObjectAddress revokedFieldAddress,
		GGObjectAddress validateAgainstMethodAddress, GGObjectAddress validateMethodAddress, GGObjectAddress authorizationTypeFieldAddress, GGObjectAddress toByteArrayMethodAddress, GGObjectAddress signMethodAddress) {

}
