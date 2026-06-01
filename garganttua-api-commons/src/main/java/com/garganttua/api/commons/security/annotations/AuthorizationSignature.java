package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Marks the field that stores the signature bytes of a signable authorization.
 * Maps to {@code ISignableAuthorizationBuilder.signature(field)}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AuthorizationSignature {

}
