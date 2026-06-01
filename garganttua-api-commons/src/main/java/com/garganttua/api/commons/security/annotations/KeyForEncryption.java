package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Field-level marker for the bytes returned by
 * {@code IKeyRealm.getKeyForEncryption().getKey().getEncoded()} — i.e.
 * the JDK-encoded form of the key used for encryption. Type: {@code byte[]}.
 *
 * <p>For asymmetric algorithms this is typically the same private key
 * as {@code @KeyForSigning}; for symmetric algorithms it is the secret
 * key shared with {@code @KeyForDecryption}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyForEncryption {

}
