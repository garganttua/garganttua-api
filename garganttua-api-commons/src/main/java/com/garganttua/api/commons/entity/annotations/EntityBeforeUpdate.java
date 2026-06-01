package com.garganttua.api.commons.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Marks a method to be called before an entity is updated.
 * The method is invoked on the entity instance before persistence.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EntityBeforeUpdate {

}
