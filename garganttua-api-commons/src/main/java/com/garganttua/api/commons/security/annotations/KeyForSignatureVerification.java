package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Field-level marker for the bytes returned by
 * {@code IKeyRealm.getKeyForSignatureVerification().getKey().getEncoded()} —
 * i.e. the JDK X.509 form of the public/secret key used to verify
 * signatures. Type: {@code byte[]}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyForSignatureVerification {

}
