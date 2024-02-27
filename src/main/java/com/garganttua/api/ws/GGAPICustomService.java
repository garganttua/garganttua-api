package com.garganttua.api.ws;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.core.GGAPICrudAccess;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GGAPICustomService {

	String path();

	String authority() default "";

	GGAPIServiceMethod method();

	GGAPICrudAccess access();
	
	String description() default "";

}
