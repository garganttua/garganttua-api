package com.garganttua.api.spec.security.authenticator;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;
import com.garganttua.reflection.GGObjectAddress;

public record GGAPIAuthenticatorInfos(
	Class<?> authenticatorType,
	Class<?>[] authenticationTypes,
	String[] authenticationInterfaces,
	Class<?> authorizationType,
	Class<?> authorizationKeyType,
	GGAPIAuthenticatorKeyUsage authorizationKeyUsage,
	boolean autoCreateAuthorizationKey,
	GGAPIKeyAlgorithm authorizationKeyAlgorithm,
	GGAPISignatureAlgorithm authorizationSignatureAlgorithm,
	int authorizationKeyLifeTime,
	TimeUnit authorizationKeyLifeTimeUnit,
	int authorizationLifeTime,
	TimeUnit authorizationLifeTimeUnit,
	int authorizationRefreshTokenLifeTime,
	TimeUnit authorizationRefreshTokenLifeTimeUnit,
	GGObjectAddress authoritiesFieldAddress,
	GGObjectAddress isAccountNonExpiredFieldAddress,
	GGObjectAddress isAccountNonLockedFieldAddress,
	GGObjectAddress isCredentialsNonExpiredFieldAddress,
	GGObjectAddress isEnabledFieldAddress,
	GGAPIAuthenticatorScope scope) {

}
