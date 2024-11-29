package com.garganttua.api.spec.security.authenticator;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.reflection.GGObjectAddress;

public record GGAPIAuthenticatorInfos (
		Class<?> authenticatorType,
		Class<?> authenticationType,
		String[] authenticationInterfaces,
		Class<?> authorizationType,
		Class<?> key,
		GGAPIAuthenticatorKeyUsage keyUsage,
		boolean autoCreateKey,
		GGAPIKeyAlgorithm keyAlgorithm,
		int keyLifeTime,
		TimeUnit keyLifeTimeUnit,
		int authorizationLifeTime,
		TimeUnit authorizationLifeTimeUnit,
		GGObjectAddress authoritiesFieldAddress,
		GGObjectAddress isAccountNonExpiredFieldAddress,
		GGObjectAddress isAccountNonLockedFieldAddress,
		GGObjectAddress isCredentialsNonExpiredFieldAddress,
		GGObjectAddress isEnabledFieldAddress) {
		
	}
