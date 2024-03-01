package com.garganttua.api.core.dto.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GGAPIDtoFieldMapping {

	String entityField();

	String fromMethod() default "";

	String toMethod() default "";

}
