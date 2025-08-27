package com.garganttua.api.spec.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.key.EncryptionMode;
import com.garganttua.api.spec.security.key.EncryptionPaddingMode;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AuthenticatorKeyRealm {

	Class<?> key();

	KeyAlgorithm keyAlgorithm() default KeyAlgorithm.RSA_4096;

	int keyLifeTime() default 24;

	TimeUnit keyLifeTimeUnit() default TimeUnit.HOURS;

	boolean autoCreateKey() default true;

	EncryptionMode encryptionMode() default EncryptionMode.ECB;

	EncryptionPaddingMode encryptionPadding() default EncryptionPaddingMode.PKCS1_PADDING;

	SignatureAlgorithm signatureAlgorithm() default SignatureAlgorithm.SHA512;

}
