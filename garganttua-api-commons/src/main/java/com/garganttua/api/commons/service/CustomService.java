package com.garganttua.api.commons.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomService {

	String path();
	
	Class<?> entity();
	
	Scope scope();
	
	TechnicalOperation operation();

	String description() default "";

}
