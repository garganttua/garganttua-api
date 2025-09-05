package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.reflection.GGObjectAddress;

public interface IRefreshableAuthorizationBuilder {

    IRefreshableAuthorizationBuilder expiration(GGObjectAddress fieldAddress);

    IRefreshableAuthorizationBuilder expiration(Field field);

    IRefreshableAuthorizationBuilder expiration(String string);

    IRefreshableAuthorizationBuilder token(Method method);

    IRefreshableAuthorizationBuilder token(String string);

    IRefreshableAuthorizationBuilder token(GGObjectAddress fieldAddress);

    IRefreshableAuthorizationBuilder create(String string);

    IRefreshableAuthorizationBuilder create(Method method);

    IRefreshableAuthorizationBuilder create(GGObjectAddress fieldAddress);

    IRefreshableAuthorizationBuilder validate(String string);

    IRefreshableAuthorizationBuilder validate(Method method);

    IRefreshableAuthorizationBuilder validate(GGObjectAddress fieldAddress);

    IAuthorizationBuilder up();

}
