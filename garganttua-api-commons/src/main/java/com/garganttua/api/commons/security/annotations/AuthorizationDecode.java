package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Marks the method that decodes an authorization from its transport/serialized form.
 * Symmetric to {@code @AuthorizationEncode}. Maps to
 * {@code IAuthorizationBuilder.decode(method)} — works on any authorization, the token
 * need NOT be refreshable.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthorizationDecode {

}
