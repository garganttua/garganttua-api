package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field-level marker for the encoded private key material of a
 * {@link Key}-marked entity. Type: {@code byte[]}. Typically the
 * PKCS#8-encoded form returned by {@code PrivateKey.getEncoded()}.
 *
 * <p>The framework stores the bytes as-is. Encrypting them at rest
 * (master-key wrapping) is the application's responsibility — typically
 * handled in the DAO layer or via {@code @EntityBeforeCreate} /
 * {@code @EntityAfterGet} hooks on the key entity itself.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyPrivateMaterial {

}
