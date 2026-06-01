package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Field-level marker for the bytes returned by
 * {@code IKeyRealm.getKeyForSigning().getKey().getEncoded()} — i.e.
 * the JDK PKCS#8 form of the private/secret key used to produce
 * signatures. Type: {@code byte[]}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyForSigning {

}
