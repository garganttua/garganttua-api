package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;

import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.reflection.GGObjectAddress;

public interface IAuthenticatorBuilder {

    IAuthenticatorBuilder autoDetect(boolean b);

    IAuthenticatorBuilder login(String string);

    IAuthenticatorBuilder login(Field field);

    IAuthenticatorBuilder login(GGObjectAddress fieldAddress);

    IAuthenticatorBuilder authorities(String string);

    IAuthenticatorBuilder authorities(Field field);

    IAuthenticatorBuilder authorities(GGObjectAddress fieldAddress);

    IAuthenticatorBuilder alwaysEnabled(boolean b);

    IAuthenticatorBuilder credentialsNonExpired(String string);

    IAuthenticatorBuilder credentialsNonExpired(Field field);

    IAuthenticatorBuilder credentialsNonExpired(GGObjectAddress fieldAddress);

    IAuthenticatorBuilder enabled(String string);

    IAuthenticatorBuilder enabled(Field field);

    IAuthenticatorBuilder enabled(GGObjectAddress fieldAddress);

    IAuthenticatorBuilder accountNonExpired(String string);

    IAuthenticatorBuilder accountNonExpired(Field field);

    IAuthenticatorBuilder accountNonExpired(GGObjectAddress fieldAddress);

    IAuthenticatorBuilder accountNonLocked(Field field);

    IAuthenticatorBuilder accountNonLocked(String string);

    IAuthenticatorBuilder accountNonLocked(GGObjectAddress fieldAddress);

    IAuthenticatorBuilder scope(AuthenticatorScope system);

    IAuthenticatorBuilder interfasse(IObjectSupplier<?> bean);

    IAuthenticatorBuilder interfasse(IInterface interfasse);

    IAuthenticatorBuilder authentication(Class<?> class1);

    IAuthenticatorAuthorizationBuilder authorization(Class<?> class1);

    IAuthorizationBuilder up();

}
