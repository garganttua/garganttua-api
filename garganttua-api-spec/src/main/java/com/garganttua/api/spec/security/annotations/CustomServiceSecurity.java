package com.garganttua.api.spec.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.spec.service.ServiceAccess;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomServiceSecurity {
	
	boolean authority() default false;

	ServiceAccess access();

}
