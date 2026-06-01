package com.garganttua.api.commons.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Marks a method to be called after an entity is deleted.
 * The method is invoked on the entity instance after removal.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EntityAfterDelete {

}
