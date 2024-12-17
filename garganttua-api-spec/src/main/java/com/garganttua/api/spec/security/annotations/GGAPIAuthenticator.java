package com.garganttua.api.spec.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorScope;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GGAPIAuthenticator {
	
	Class<?> authorization() default void.class;
	
	int authorizationLifeTime() default 60;
	
	TimeUnit authorizationLifeTimeUnit() default TimeUnit.MINUTES;
	
	String[] interfaces() default {};
	
	Class<?>[] authentications() default {void.class};

	Class<?> authorizationKey() default void.class;

	GGAPIAuthenticatorKeyUsage authorizationKeyUsage() default GGAPIAuthenticatorKeyUsage.oneForTenant;

	boolean autoCreateAuthorizationKey() default false;

	GGAPIKeyAlgorithm authorizationKeyAlgorithm() default GGAPIKeyAlgorithm.RSA_4096;

	TimeUnit authorizationKeyLifeTimeUnit() default TimeUnit.MINUTES;

	int authorizationKeyLifeTime() default 60;

  GGAPIAuthenticatorScope scope() default GGAPIAuthenticatorScope.tenant;

}
