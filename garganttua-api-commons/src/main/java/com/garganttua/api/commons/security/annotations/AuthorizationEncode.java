package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Marks the method that encodes an authorization to its transport/serialized form
 * (e.g. a JWT {@code header.payload.signature}). The encoded form becomes the output
 * of {@code authenticate} / {@code refreshAuthorization}. Maps to
 * {@code IAuthorizationBuilder.encode(method)} — works on any authorization, the token
 * need NOT be refreshable.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthorizationEncode {

}
