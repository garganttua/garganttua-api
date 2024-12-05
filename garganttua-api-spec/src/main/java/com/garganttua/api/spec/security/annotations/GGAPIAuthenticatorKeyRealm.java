package com.garganttua.api.spec.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.key.GGAPIEncryptionMode;
import com.garganttua.api.spec.security.key.GGAPIEncryptionPaddingMode;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GGAPIAuthenticatorKeyRealm {

	Class<?> key();

	GGAPIKeyAlgorithm keyAlgorithm() default GGAPIKeyAlgorithm.RSA_4096;

	int keyLifeTime() default 24;

	TimeUnit keyLifeTimeUnit() default TimeUnit.HOURS;

	boolean autoCreateKey() default true;

	GGAPIEncryptionMode encryptionMode() default GGAPIEncryptionMode.ECB;

	GGAPIEncryptionPaddingMode encryptionPadding() default GGAPIEncryptionPaddingMode.PKCS1_PADDING;

	GGAPISignatureAlgorithm signatureAlgorithm() default GGAPISignatureAlgorithm.SHA512;

}
