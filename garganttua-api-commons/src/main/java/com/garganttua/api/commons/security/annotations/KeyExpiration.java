package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field-level marker for the expiration timestamp of a {@link Key}-marked
 * entity. Type: {@code java.util.Date} or {@code java.time.Instant}.
 * A key whose expiration is in the past is ignored by the auto-create
 * lookup, which then generates a fresh one.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyExpiration {

}
