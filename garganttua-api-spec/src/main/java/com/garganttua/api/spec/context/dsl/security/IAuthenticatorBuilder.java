package com.garganttua.api.spec.context.dsl.security;

import java.lang.reflect.Field;

import com.garganttua.api.spec.security.context.IAuthenticatorContext;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthenticatorBuilder<E> extends IAutomaticLinkedBuilder<IAuthenticatorBuilder<E>, IDomainSecurityBuilder<E>, IAuthenticatorContext> {

    IAuthenticatorBuilder<E> login(String string) throws ApiException;

    IAuthenticatorBuilder<E> login(Field field) throws ApiException;

    IAuthenticatorBuilder<E> login(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> authorities(String string) throws ApiException;

    IAuthenticatorBuilder<E> authorities(Field field) throws ApiException;

    IAuthenticatorBuilder<E> authorities(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> alwaysEnabled(boolean b);

    IAuthenticatorBuilder<E> credentialsNonExpired(String string) throws ApiException;

    IAuthenticatorBuilder<E> credentialsNonExpired(Field field) throws ApiException;

    IAuthenticatorBuilder<E> credentialsNonExpired(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> enabled(String string) throws ApiException;

    IAuthenticatorBuilder<E> enabled(Field field) throws ApiException;

    IAuthenticatorBuilder<E> enabled(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> accountNonExpired(String string) throws ApiException;

    IAuthenticatorBuilder<E> accountNonExpired(Field field) throws ApiException;

    IAuthenticatorBuilder<E> accountNonExpired(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> accountNonLocked(Field field) throws ApiException;

    IAuthenticatorBuilder<E> accountNonLocked(String string) throws ApiException;

    IAuthenticatorBuilder<E> accountNonLocked(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> scope(AuthenticatorScope system);

    IAuthenticatorBuilder<E> authentication(IAuthenticationBuilder authentication) throws ApiException;

    IAuthenticatorAuthorizationBuilder<E> authorization(IDomainBuilder<E> authorization);

}
