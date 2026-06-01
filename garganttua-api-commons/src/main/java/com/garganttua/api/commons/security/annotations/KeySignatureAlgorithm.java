package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Field-level marker for the signature algorithm name of a
 * {@link Key}-marked entity (e.g. {@code "SHA256withRSA"},
 * {@code "SHA512withECDSA"}). Type: {@code String}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeySignatureAlgorithm {

}
