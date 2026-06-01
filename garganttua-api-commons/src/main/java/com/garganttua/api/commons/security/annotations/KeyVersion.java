package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Field-level marker for the integer version of a {@link Key}-marked
 * entity — corresponds to {@code IKeyRealm.getVersion()}. The framework
 * increments this value when the realm is rotated. Type: {@code int} /
 * {@code Integer}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyVersion {

}
