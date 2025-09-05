package com.garganttua.api.spec.engine;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;

public interface IAuthenticatorKeyBuilder {

    IAuthenticatorKeyBuilder usage(AuthenticatorKeyUsage oneforall);

    IAuthenticatorKeyBuilder algorithm(KeyAlgorithm hmacSha512512);

    IAuthenticatorKeyBuilder signatureAlgorithm(SignatureAlgorithm hmacSha512);

    IAuthenticatorKeyBuilder lifeTime(int i, TimeUnit days);

    IAuthenticatorKeyBuilder autoCreate(boolean b);

    IAuthenticatorBuilder up();

}
