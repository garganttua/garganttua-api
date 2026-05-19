package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field-level marker for the encoded public key material of a
 * {@link Key}-marked entity. Type: {@code byte[]}. Typically the
 * X.509-encoded form returned by {@code PublicKey.getEncoded()}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyPublicMaterial {

}
