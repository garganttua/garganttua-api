package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;

/**
 * Method binder builder parented to {@link IAuthenticatorBuilder}. Backs the
 * authenticator's custom token-production (mint) method declared via
 * {@code .authorization(issuer, "method")}: {@code .withParam(i, supplier)} binds
 * the method's parameters, {@code .up()} returns the authenticator builder.
 *
 * @param <E> the authenticator entity type
 */
public interface IAuthenticatorMethodBinderBuilder<E> extends
        IMethodBinderBuilder<Object, IAuthenticatorMethodBinderBuilder<E>, IAuthenticatorBuilder<E>, IMethodBinder<Object>> {
}
