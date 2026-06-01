package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Field-level marker for the last-rotation timestamp of a
 * {@link Key}-marked entity. Corresponds to {@code IKeyRealm.rotate()}:
 * the framework writes this field when the realm is rotated and reads
 * it back to decide when the next rotation is due. Type:
 * {@code Date} / {@code Instant} / {@code Long} — same shape as
 * {@link KeyExpiration}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyRotate {

}
