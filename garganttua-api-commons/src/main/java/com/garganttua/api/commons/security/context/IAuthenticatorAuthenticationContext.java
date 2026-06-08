package com.garganttua.api.commons.security.context;

/**
 * Context produced by {@link com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthenticationBuilder}.
 *
 * <p>The per-authentication sub-builder is a thin navigation node: the token
 * domain and the mint binder it declares are stored on the parent authenticator
 * (one per authenticator), so this context carries no aggregated state. It exists
 * to satisfy the {@code IAutomaticLinkedBuilder} contract — giving the
 * authentication sub-builder a proper {@code up()} / {@code build()} lifecycle.
 */
public interface IAuthenticatorAuthenticationContext {
}
