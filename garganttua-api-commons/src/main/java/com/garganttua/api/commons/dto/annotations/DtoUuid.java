package com.garganttua.api.commons.dto.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Marks the field on a DTO that holds the entity UUID.
 * Maps to {@code IDtoBuilder.uuid(field)}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DtoUuid {

}
