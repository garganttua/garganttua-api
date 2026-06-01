package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.core.crypto.SignatureAlgorithm;

@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Authenticator {

	Class<?> authorization() default void.class;

	int authorizationLifeTime() default 60;

	TimeUnit authorizationLifeTimeUnit() default TimeUnit.MINUTES;

	int authorizationRefreshTokenLifeTime() default 120;

	TimeUnit authorizationRefreshTokenLifeTimeUnit() default TimeUnit.MINUTES;

	Class<?>[] authentications() default {void.class};

	Class<?> authorizationKey() default void.class;

	AuthenticatorKeyUsage authorizationKeyUsage() default AuthenticatorKeyUsage.oneForTenant;

	/**
	 * Algorithm name resolved at scan time against the user-provided
	 * {@code IKeyAlgorithm} registry. Empty string defers resolution to the
	 * fluent DSL.
	 */
	String authorizationKeyAlgorithm() default "";

	SignatureAlgorithm authorizationSignatureAlgorithm() default SignatureAlgorithm.SHA512;

	TimeUnit authorizationKeyLifeTimeUnit() default TimeUnit.MINUTES;

	int authorizationKeyLifeTime() default 60;

	AuthenticatorScope scope() default AuthenticatorScope.tenant;

}
