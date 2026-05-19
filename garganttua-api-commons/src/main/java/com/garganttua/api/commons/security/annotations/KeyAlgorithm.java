package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field-level marker for the algorithm identifier of a {@link Key}-marked
 * entity (e.g. {@code "RSA"}, {@code "EC"}). Type: {@code String}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyAlgorithm {

}
