package com.garganttua.api.spec.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.spec.GGAPIEntityOperation;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GGAPICustomService {

	String path();

//	String authority() default "";

	GGAPIEntityOperation operation();

//	GGAPIServiceAccess access();
	
	String description() default "";

}
