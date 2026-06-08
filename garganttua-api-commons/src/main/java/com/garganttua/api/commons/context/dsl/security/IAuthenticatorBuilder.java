package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IField;

import com.garganttua.api.commons.security.context.IAuthenticatorContext;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public interface IAuthenticatorBuilder<E> extends IAutomaticLinkedBuilder<IAuthenticatorBuilder<E>, IDomainSecurityBuilder<E>, IAuthenticatorContext> {

    IAuthenticatorBuilder<E> login(String string) throws ApiException;

    IAuthenticatorBuilder<E> login(IField field) throws ApiException;

    IAuthenticatorBuilder<E> login(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> authorities(String string) throws ApiException;

    IAuthenticatorBuilder<E> authorities(IField field) throws ApiException;

    IAuthenticatorBuilder<E> authorities(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> alwaysEnabled(boolean b);

    IAuthenticatorBuilder<E> credentialsNonExpired(String string) throws ApiException;

    IAuthenticatorBuilder<E> credentialsNonExpired(IField field) throws ApiException;

    IAuthenticatorBuilder<E> credentialsNonExpired(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> enabled(String string) throws ApiException;

    IAuthenticatorBuilder<E> enabled(IField field) throws ApiException;

    IAuthenticatorBuilder<E> enabled(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> accountNonExpired(String string) throws ApiException;

    IAuthenticatorBuilder<E> accountNonExpired(IField field) throws ApiException;

    IAuthenticatorBuilder<E> accountNonExpired(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> accountNonLocked(IField field) throws ApiException;

    IAuthenticatorBuilder<E> accountNonLocked(String string) throws ApiException;

    IAuthenticatorBuilder<E> accountNonLocked(ObjectAddress fieldAddress) throws ApiException;

    IAuthenticatorBuilder<E> scope(AuthenticatorScope scope);

    /**
     * Links an authentication (the verify cascade) to this authenticator and
     * returns the per-authentication sub-builder where the authorization is
     * declared — both the token domain {@code .authorization(domain)} and the
     * mint-side {@code .authorization(issuer, "method")}. Call {@code .up()} on the
     * returned sub-builders to come back to this authenticator.
     */
    IAuthenticatorAuthenticationBuilder<E> authentication(IAuthenticationBuilder<?> authentication) throws ApiException;

    IAuthenticationBuilder<IAuthenticatorBuilder<E>> authentication(ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException;

    IAuthenticationBuilder<IAuthenticatorBuilder<E>> authentication(IClass<?> authenticationClass) throws ApiException;

}
