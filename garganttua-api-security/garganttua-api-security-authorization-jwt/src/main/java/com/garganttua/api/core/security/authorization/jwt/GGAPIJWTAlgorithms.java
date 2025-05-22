package com.garganttua.api.core.security.authorization.jwt;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;

import lombok.Getter;

@Getter
public enum GGAPIJWTAlgorithms {

    HS256(GGAPIKeyAlgorithm.HMAC_SHA512_256, GGAPISignatureAlgorithm.HMAC_SHA512),
    HS384(GGAPIKeyAlgorithm.HMAC_SHA512_384, GGAPISignatureAlgorithm.HMAC_SHA512),
    HS512(GGAPIKeyAlgorithm.HMAC_SHA512_512, GGAPISignatureAlgorithm.HMAC_SHA512),
    RS256(GGAPIKeyAlgorithm.RSA_4096, GGAPISignatureAlgorithm.SHA256),
    RS384(GGAPIKeyAlgorithm.RSA_4096, GGAPISignatureAlgorithm.SHA384),
    RS512(GGAPIKeyAlgorithm.RSA_4096, GGAPISignatureAlgorithm.SHA512),
    ES256(GGAPIKeyAlgorithm.EC_256, GGAPISignatureAlgorithm.SHA256),
    ES384(GGAPIKeyAlgorithm.EC_384, GGAPISignatureAlgorithm.SHA384),
    ES512(GGAPIKeyAlgorithm.EC_512, GGAPISignatureAlgorithm.SHA512);

    private GGAPIKeyAlgorithm keyAlgorithm;
    private GGAPISignatureAlgorithm signatureAlgorithm;

    GGAPIJWTAlgorithms(GGAPIKeyAlgorithm keyAlgorithm, GGAPISignatureAlgorithm signatureAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public static GGAPIJWTAlgorithms from(GGAPIKeyAlgorithm keyAlgorithm, GGAPISignatureAlgorithm signatureAlgorithm)
            throws GGAPISecurityException {
        for (GGAPIJWTAlgorithms algo : GGAPIJWTAlgorithms.values()) {
            if (algo.getKeyAlgorithm() == keyAlgorithm && algo.getSignatureAlgorithm() == signatureAlgorithm) {
                return algo;
            }
        }
        throw new GGAPISecurityException(
                GGAPIExceptionCode.GENERIC_SECURITY_ERROR,
                "Unsupported combination: keyAlgorithm=" + keyAlgorithm + ", signatureAlgorithm=" + signatureAlgorithm);
    }

    @Override
    public String toString() {
        return this.name();
    }

    public static GGAPIJWTAlgorithms fromString(String alg) throws IllegalArgumentException, GGAPISecurityException {
        for (GGAPIJWTAlgorithms value : GGAPIJWTAlgorithms.values()) {
            if (value.name().equalsIgnoreCase(alg)) {
                return value;
            }
        }
        throw new GGAPISecurityException(
                GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Unsupported JWT algorithm: " + alg);
    }
}