package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field-level marker for the integer version of a {@link Key}-marked
 * entity — corresponds to {@code IKeyRealm.getVersion()}. The framework
 * increments this value when the realm is rotated. Type: {@code int} /
 * {@code Integer}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyVersion {

}
