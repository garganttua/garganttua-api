package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.reflection.GGObjectAddress;

public interface IAuthenticatorBuilder extends IAutomaticLinkedBuilder<Object, IDomainBuilder, IAuthenticatorBuilder> {

    IAuthenticatorBuilder login(String string) throws CoreException;

    IAuthenticatorBuilder login(Field field) throws CoreException;

    IAuthenticatorBuilder login(GGObjectAddress fieldAddress) throws CoreException;

    IAuthenticatorBuilder authorities(String string) throws CoreException;

    IAuthenticatorBuilder authorities(Field field) throws CoreException;

    IAuthenticatorBuilder authorities(GGObjectAddress fieldAddress) throws CoreException;

    IAuthenticatorBuilder alwaysEnabled(boolean b);

    IAuthenticatorBuilder credentialsNonExpired(String string) throws CoreException;

    IAuthenticatorBuilder credentialsNonExpired(Field field) throws CoreException;

    IAuthenticatorBuilder credentialsNonExpired(GGObjectAddress fieldAddress) throws CoreException;

    IAuthenticatorBuilder enabled(String string) throws CoreException;

    IAuthenticatorBuilder enabled(Field field) throws CoreException;

    IAuthenticatorBuilder enabled(GGObjectAddress fieldAddress) throws CoreException;

    IAuthenticatorBuilder accountNonExpired(String string) throws CoreException;

    IAuthenticatorBuilder accountNonExpired(Field field) throws CoreException;

    IAuthenticatorBuilder accountNonExpired(GGObjectAddress fieldAddress) throws CoreException;

    IAuthenticatorBuilder accountNonLocked(Field field) throws CoreException;

    IAuthenticatorBuilder accountNonLocked(String string) throws CoreException;

    IAuthenticatorBuilder accountNonLocked(GGObjectAddress fieldAddress) throws CoreException;

    IAuthenticatorBuilder scope(AuthenticatorScope system);

    IAuthenticatorBuilder authentication(Class<?> authenticationClass);

    IAuthenticatorAuthorizationBuilder authorization(IDomainBuilder authorization);

}
