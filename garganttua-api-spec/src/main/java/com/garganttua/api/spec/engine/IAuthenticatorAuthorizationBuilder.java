package com.garganttua.api.spec.engine;

import java.util.concurrent.TimeUnit;

public interface IAuthenticatorAuthorizationBuilder {

    IAuthenticatorAuthorizationBuilder lifeTime(int i, TimeUnit days);

    IAuthenticatorAuthorizationBuilder refreshLifeTime(int i, TimeUnit days);

    IAuthenticatorKeyBuilder key(Class<?> class1);

}
