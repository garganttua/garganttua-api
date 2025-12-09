package com.garganttua.api.spec.context.dsl.security;

import java.lang.reflect.Field;

import com.garganttua.api.spec.context.IAuthenticatorContext;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthenticatorBuilder<E> extends IAutomaticLinkedBuilder<IAuthenticatorBuilder<E>, IDomainSecurityBuilder<E>, IAuthenticatorContext> {

    IAuthenticatorBuilder<E> login(String string) throws DslException;

    IAuthenticatorBuilder<E> login(Field field) throws DslException;

    IAuthenticatorBuilder<E> login(ObjectAddress fieldAddress) throws DslException;

    IAuthenticatorBuilder<E> authorities(String string) throws DslException;

    IAuthenticatorBuilder<E> authorities(Field field) throws DslException;

    IAuthenticatorBuilder<E> authorities(ObjectAddress fieldAddress) throws DslException;

    IAuthenticatorBuilder<E> alwaysEnabled(boolean b);

    IAuthenticatorBuilder<E> credentialsNonExpired(String string) throws DslException;

    IAuthenticatorBuilder<E> credentialsNonExpired(Field field) throws DslException;

    IAuthenticatorBuilder<E> credentialsNonExpired(ObjectAddress fieldAddress) throws DslException;

    IAuthenticatorBuilder<E> enabled(String string) throws DslException;

    IAuthenticatorBuilder<E> enabled(Field field) throws DslException;

    IAuthenticatorBuilder<E> enabled(ObjectAddress fieldAddress) throws DslException;

    IAuthenticatorBuilder<E> accountNonExpired(String string) throws DslException;

    IAuthenticatorBuilder<E> accountNonExpired(Field field) throws DslException;

    IAuthenticatorBuilder<E> accountNonExpired(ObjectAddress fieldAddress) throws DslException;

    IAuthenticatorBuilder<E> accountNonLocked(Field field) throws DslException;

    IAuthenticatorBuilder<E> accountNonLocked(String string) throws DslException;

    IAuthenticatorBuilder<E> accountNonLocked(ObjectAddress fieldAddress) throws DslException;

    IAuthenticatorBuilder<E> scope(AuthenticatorScope system);

    IAuthenticatorBuilder<E> authentication(IAuthenticationBuilder authentication) throws DslException;

    IAuthenticatorAuthorizationBuilder<E> authorization(IDomainBuilder<E> authorization);

}
