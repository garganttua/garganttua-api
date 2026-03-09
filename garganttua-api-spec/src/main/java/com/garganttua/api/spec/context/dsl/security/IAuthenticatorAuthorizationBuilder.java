package com.garganttua.api.spec.context.dsl.security;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.context.IAuthenticatorAuthorizationContext;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IAuthenticatorAuthorizationBuilder<E> extends IAutomaticLinkedBuilder<IAuthenticatorAuthorizationBuilder<E>, IAuthenticatorBuilder<E>, IAuthenticatorAuthorizationContext>{

    IAuthenticatorAuthorizationBuilder<E> lifeTime(int i, TimeUnit days);

    IAuthenticatorAuthorizationBuilder<E> refreshLifeTime(int i, TimeUnit days);

    IAuthenticatorAuthorizationKeyBuilder<E> key(IDomainBuilder<E> key);

}
