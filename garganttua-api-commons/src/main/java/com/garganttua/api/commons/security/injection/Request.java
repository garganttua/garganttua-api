package com.garganttua.api.commons.security.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.core.reflection.annotations.Indexed;
import com.garganttua.core.reflection.annotations.Reflected;

/**
 * Declarative injection marker for a security-method parameter — the annotation
 * dual of {@code .withParam(i, new RequestSupplierBuilder())}. Resolved by
 * {@code RequestElementResolver}.
 */
@Indexed
@Reflected
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Request {
}
