package com.garganttua.api.core.dto.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.core.dao.GGAPIDao;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GGAPIDto {
	String db() default GGAPIDao.MONGO;	
	Class<?> entityClass();
}
