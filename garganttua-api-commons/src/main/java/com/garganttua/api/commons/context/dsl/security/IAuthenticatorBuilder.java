package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.core.reflection.IField;

import com.garganttua.api.commons.security.context.IAuthenticatorContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

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

    IAuthenticatorBuilder<E> authentication(IAuthenticationBuilder authentication) throws ApiException;

    IAuthenticatorAuthorizationBuilder<E> authorization(IDomainBuilder<E> authorizationDomain);

}
