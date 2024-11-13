package com.garganttua.api.spec.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.spec.GGAPIMethod;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GGAPICustomService {

	String path();
	
	String entityName();
	
	boolean actionOnAllEntities();
	
	GGAPIMethod method();

	String description() default "";

}
