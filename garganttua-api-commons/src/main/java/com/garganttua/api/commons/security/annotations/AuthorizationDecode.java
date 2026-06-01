package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Marks the method that decodes a refreshable authorization from its serialized form.
 * Maps to {@code IRefreshableAuthorizationBuilder.decode(method)}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthorizationDecode {

}
