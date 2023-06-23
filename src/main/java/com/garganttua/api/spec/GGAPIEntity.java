package com.garganttua.api.spec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.repository.dao.GGAPIDao;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GGAPIEntity {

	String dto();

	GGAPIDao db() default GGAPIDao.mongo;
	
	boolean authorize_creation() default true;

	boolean authorize_read_all() default true;

	boolean authorize_read_one() default true;

	boolean authorize_update_one() default true;

	boolean authorize_delete_one() default true;

	boolean authorize_delete_all() default true;

	boolean authorize_count() default true;
	
	String controller() default "";
	
	String business() default "";
	
	String ws() default "";
	
	String connector() default "";

	String repository() default "";
	
	String dao() default "";

	String openApiSchemas() default "";
	
	String eventPublisher() default "";

	String domain();

}
