package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.reflection.GGObjectAddress;

public interface IAuthorizationBuilder {

    IAuthorizationBuilder autoDetect(boolean b);

    IAuthorizationBuilder type(Field field);

    IAuthorizationBuilder type(String string);

    IAuthorizationBuilder type(GGObjectAddress fieldAddress);

    IAuthorizationBuilder authorities(String string);

    IAuthorizationBuilder authorities(Field field);

    IAuthorizationBuilder authorities(GGObjectAddress fieldAddress);

    IAuthorizationBuilder creation(String string);

    IAuthorizationBuilder creation(Field field);

    IAuthorizationBuilder creation(GGObjectAddress fieldAddress);

    IAuthorizationBuilder expiration(GGObjectAddress fieldAddress);

    IAuthorizationBuilder expiration(Field field);

    IAuthorizationBuilder expiration(String string);

    IAuthorizationBuilder revoked(String string);

    IAuthorizationBuilder revoked(Field field);

    IAuthorizationBuilder revoked(GGObjectAddress fieldAddress);

    IAuthorizationBuilder toByteArray(Method method);

    IAuthorizationBuilder toByteArray(String string);

    IAuthorizationBuilder toByteArray(GGObjectAddress fieldAddress);

    IAuthorizationBuilder validate(String string);

    IAuthorizationBuilder validate(Method method);

    IAuthorizationBuilder validate(GGObjectAddress fieldAddress);

    IAuthorizationBuilder validateAgainst(String string);

    IAuthorizationBuilder validateAgainst(Method method);

    IAuthorizationBuilder validateAgainst(GGObjectAddress fieldAddress);

    ISignableAuthorizationBuilder signable();

    IRefreshableAuthorizationBuilder refreshable();

    IDomainBuilder up();



}
