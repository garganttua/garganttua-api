package com.garganttua.api.spec.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.spec.service.GGAPIServiceAccess;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GGAPICustomServiceSecurity {
	
	String authority() default "";

	GGAPIServiceAccess access();

}
