package com.garganttua.api.spec.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.spec.operation.Scope;
import com.garganttua.api.spec.operation.TechnicalOperation;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomService {

	String path();
	
	Class<?> entity();
	
	Scope scope();
	
	TechnicalOperation operation();

	String description() default "";

}
