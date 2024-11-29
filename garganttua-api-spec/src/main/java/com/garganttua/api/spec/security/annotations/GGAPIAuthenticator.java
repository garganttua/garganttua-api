package com.garganttua.api.spec.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GGAPIAuthenticator {
	
	Class<?> authorization() default void.class;
	
	int authorizationLifeTime() default 60;
	
	TimeUnit authorizationLifeTimeUnit() default TimeUnit.MINUTES;
	
	String[] interfaces() default {};
	
	Class<?> authentication() default void.class;

	Class<?> key() default void.class;

	GGAPIAuthenticatorKeyUsage keyUsage() default GGAPIAuthenticatorKeyUsage.oneForTenant;

	boolean autoCreateKey() default false;

	GGAPIKeyAlgorithm keyAlgorithm() default GGAPIKeyAlgorithm.RSA_4096;

	TimeUnit keyLifeTimeUnit() default TimeUnit.MINUTES;

	int keyLifeTime() default 60;

}
