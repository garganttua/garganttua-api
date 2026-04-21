package com.garganttua.api.core.security.authorization.jwt;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.commons.CoreExceptionCode;

import com.garganttua.api.commons.security.key.KeyAlgorithm;
import com.garganttua.api.commons.security.key.SignatureAlgorithm;

import lombok.Getter;

@Getter
public enum JWTAlgorithms {

    HS256(KeyAlgorithm.HMAC_SHA512_256, SignatureAlgorithm.HMAC_SHA512),
    HS384(KeyAlgorithm.HMAC_SHA512_384, SignatureAlgorithm.HMAC_SHA512),
    HS512(KeyAlgorithm.HMAC_SHA512_512, SignatureAlgorithm.HMAC_SHA512),
    RS256(KeyAlgorithm.RSA_4096, SignatureAlgorithm.SHA256),
    RS384(KeyAlgorithm.RSA_4096, SignatureAlgorithm.SHA384),
    RS512(KeyAlgorithm.RSA_4096, SignatureAlgorithm.SHA512),
    ES256(KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256),
    ES384(KeyAlgorithm.EC_384, SignatureAlgorithm.SHA384),
    ES512(KeyAlgorithm.EC_512, SignatureAlgorithm.SHA512);

    private KeyAlgorithm keyAlgorithm;
    private SignatureAlgorithm signatureAlgorithm;

    JWTAlgorithms(KeyAlgorithm keyAlgorithm, SignatureAlgorithm signatureAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public static JWTAlgorithms from(KeyAlgorithm keyAlgorithm, SignatureAlgorithm signatureAlgorithm)
            throws SecurityException {
        for (JWTAlgorithms algo : JWTAlgorithms.values()) {
            if (algo.getKeyAlgorithm() == keyAlgorithm && algo.getSignatureAlgorithm() == signatureAlgorithm) {
                return algo;
            }
        }
        throw new SecurityException(
                CoreExceptionCode.GENERIC_SECURITY_ERROR,
                "Unsupported combination: keyAlgorithm=" + keyAlgorithm + ", signatureAlgorithm=" + signatureAlgorithm);
    }

    @Override
    public String toString() {
        return this.name();
    }

    public static JWTAlgorithms fromString(String alg) throws IllegalArgumentException, SecurityException {
        for (JWTAlgorithms value : JWTAlgorithms.values()) {
            if (value.name().equalsIgnoreCase(alg)) {
                return value;
            }
        }
        throw new SecurityException(
                CoreExceptionCode.GENERIC_SECURITY_ERROR, "Unsupported JWT algorithm: " + alg);
    }
}