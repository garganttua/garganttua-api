package com.garganttua.api.commons.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {

	boolean creation() default true;

	boolean readAll() default true;

	boolean readOne() default true;

	boolean update() default true;

	boolean deleteOne() default true;

	boolean deleteAll() default true;

	String[] interfaces() default {};

	String eventPublisher() default "";

	String domain() default "";
}
