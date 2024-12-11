package com.garganttua.api.core.security.authentication.pin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GGAPIAuthenticatorPinErrorCounter {

	int maxErrorNumber() default 3;
	
}
