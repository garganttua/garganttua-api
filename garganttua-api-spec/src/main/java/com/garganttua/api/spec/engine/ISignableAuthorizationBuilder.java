package com.garganttua.api.spec.engine;

import java.lang.reflect.Method;

import com.garganttua.reflection.GGObjectAddress;

public interface ISignableAuthorizationBuilder {

    ISignableAuthorizationBuilder sign(String string);

    ISignableAuthorizationBuilder sign(Method method);

    ISignableAuthorizationBuilder sign(GGObjectAddress fieldAddress);

    IAuthorizationBuilder up();

}
