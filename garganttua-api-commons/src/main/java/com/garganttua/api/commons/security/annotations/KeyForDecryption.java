package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Field-level marker for the bytes returned by
 * {@code IKeyRealm.getKeyForDecryption().getKey().getEncoded()} — i.e.
 * the JDK-encoded form of the key used for decryption. Type: {@code byte[]}.
 *
 * <p>For asymmetric algorithms this is typically the same public key as
 * {@code @KeyForSignatureVerification}; for symmetric algorithms it is
 * the secret key shared with {@code @KeyForEncryption}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyForDecryption {

}
