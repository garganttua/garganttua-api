package com.garganttua.api.spec.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GGAPIEntity {
	
	boolean allow_creation() default true;

	boolean allow_read_all() default true;

	boolean allow_read_one() default true;

	boolean allow_update_one() default true;

	boolean allow_delete_one() default true;

	boolean allow_delete_all() default true;

	boolean allow_count() default true;
	
	String[] interfaces() default {};

	String[] daos() default {};
	
	String openApiSchemas() default "";
	
	String eventPublisher() default "";

	String domain();
}
