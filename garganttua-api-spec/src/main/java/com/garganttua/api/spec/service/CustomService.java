package com.garganttua.api.spec.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.spec.Method;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomService {

	String path();
	
	Class<?> entity();
	
	boolean actionOnAllEntities();
	
	Method method();

	String description() default "";

}
