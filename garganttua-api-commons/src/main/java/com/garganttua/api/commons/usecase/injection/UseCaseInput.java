package com.garganttua.api.commons.usecase.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.core.reflection.annotations.Indexed;
import com.garganttua.core.reflection.annotations.Reflected;

/**
 * Marks the parameter of a use case's bound method that should receive the deserialized request
 * body (the use case's {@code InputType}). The use-case counterpart of {@code @Login} / {@code @Caller};
 * resolved by {@code UseCaseInputElementResolver}. The method stays "completely free" — it declares
 * this parameter (and any other supplier-backed ones) in any order.
 */
@Indexed
@Reflected
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UseCaseInput {
}
