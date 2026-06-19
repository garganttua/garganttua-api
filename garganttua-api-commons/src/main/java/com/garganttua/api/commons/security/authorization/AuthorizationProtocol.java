package com.garganttua.api.commons.security.authorization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Marks a class as an authorization protocol that should be auto-discovered and
 * registered on the {@link com.garganttua.api.commons.context.IApi} global protocol pool.
 * <p>
 * Requirements:
 * <ul>
 *   <li>The annotated class must implement {@link IAuthorizationProtocol}</li>
 *   <li>The annotated class must expose a public no-arg constructor</li>
 *   <li>The containing package must be declared via
 *       {@code IApiBuilder.withPackage(...)} and auto-detection must be enabled
 *       with {@code IApiBuilder.autoDetect(true)}</li>
 * </ul>
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AuthorizationProtocol {
}
