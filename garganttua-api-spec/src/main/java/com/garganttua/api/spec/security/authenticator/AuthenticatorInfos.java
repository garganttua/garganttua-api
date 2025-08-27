package com.garganttua.api.spec.security.authenticator;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.reflection.GGObjectAddress;

public record AuthenticatorInfos(
	Class<?> authenticatorType,
	Class<?>[] authenticationTypes,
	String[] authenticationInterfaces,
	Class<?> authorizationType,
	Class<?> authorizationKeyType,
	AuthenticatorKeyUsage authorizationKeyUsage,
	boolean autoCreateAuthorizationKey,
	KeyAlgorithm authorizationKeyAlgorithm,
	SignatureAlgorithm authorizationSignatureAlgorithm,
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
	AuthenticatorScope scope) {

}
