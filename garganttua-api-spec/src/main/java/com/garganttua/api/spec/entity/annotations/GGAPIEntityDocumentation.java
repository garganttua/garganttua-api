package com.garganttua.api.spec.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GGAPIEntityDocumentation {
	String general() default "";
	String readAll() default "";
	String readOne() default "";
	String createOne() default "";
	String updateOne() default "";
	String deleteOne() default "";
	String deleteAll() default "";
}
