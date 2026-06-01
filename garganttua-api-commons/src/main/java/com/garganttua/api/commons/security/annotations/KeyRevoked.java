package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Field-level marker for the revocation flag of a {@link Key}-marked
 * entity. Type: {@code boolean} (or {@code Boolean}). A key whose flag
 * is {@code true} is ignored by the auto-create lookup and a fresh one
 * is generated.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyRevoked {

}
