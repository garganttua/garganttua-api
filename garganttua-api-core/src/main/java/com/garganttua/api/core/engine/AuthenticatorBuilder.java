package com.garganttua.api.core.engine;

import java.lang.reflect.Field;

import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.spec.engine.IAuthenticatorBuilder;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.reflection.GGObjectAddress;

public class AuthenticatorBuilder implements IAuthenticatorBuilder {

    public AuthenticatorBuilder(DomainBuilder domainBuilder) {
        //TODO Auto-generated constructor stub
    }

    @Override
    public IAuthenticatorBuilder autoDetect(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'autoDetect'");
    }

    @Override
    public IAuthenticatorBuilder login(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public IAuthenticatorBuilder login(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public IAuthenticatorBuilder login(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public IAuthenticatorBuilder authorities(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorities'");
    }

    @Override
    public IAuthenticatorBuilder authorities(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorities'");
    }

    @Override
    public IAuthenticatorBuilder authorities(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorities'");
    }

    @Override
    public IAuthenticatorBuilder alwaysEnabled(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'alwaysEnabled'");
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'credentialsNonExpired'");
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'credentialsNonExpired'");
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'credentialsNonExpired'");
    }

    @Override
    public IAuthenticatorBuilder enabled(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'enabled'");
    }

    @Override
    public IAuthenticatorBuilder enabled(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'enabled'");
    }

    @Override
    public IAuthenticatorBuilder enabled(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'enabled'");
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'accountNonExpired'");
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'accountNonExpired'");
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'accountNonExpired'");
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'accountNonLocked'");
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'accountNonLocked'");
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'accountNonLocked'");
    }

    @Override
    public IAuthenticatorBuilder scope(AuthenticatorScope system) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'scope'");
    }

    @Override
    public IAuthenticatorBuilder interfasse(IObjectSupplier<?> bean) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'interfasse'");
    }

    @Override
    public IAuthenticatorBuilder interfasse(IInterface interfasse) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'interfasse'");
    }

    @Override
    public IAuthenticatorBuilder authentication(Class<?> class1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authentication'");
    }

    @Override
    public IAuthenticatorAuthorizationBuilder authorization(Class<?> class1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorization'");
    }

    @Override
    public IAuthorizationBuilder up() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'up'");
    }

}
