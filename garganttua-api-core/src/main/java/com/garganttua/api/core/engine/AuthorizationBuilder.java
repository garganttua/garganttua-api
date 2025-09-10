package com.garganttua.api.core.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.engine.ISignableAuthorizationBuilder;
import com.garganttua.reflection.GGObjectAddress;

public class AuthorizationBuilder implements IAuthorizationBuilder {

    public AuthorizationBuilder(DomainBuilder domainBuilder) {
        //TODO Auto-generated constructor stub
    }

    @Override
    public IAuthorizationBuilder autoDetect(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'autoDetect'");
    }

    @Override
    public IAuthorizationBuilder type(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'type'");
    }

    @Override
    public IAuthorizationBuilder type(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'type'");
    }

    @Override
    public IAuthorizationBuilder type(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'type'");
    }

    @Override
    public IAuthorizationBuilder authorities(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorities'");
    }

    @Override
    public IAuthorizationBuilder authorities(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorities'");
    }

    @Override
    public IAuthorizationBuilder authorities(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorities'");
    }

    @Override
    public IAuthorizationBuilder creation(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'creation'");
    }

    @Override
    public IAuthorizationBuilder creation(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'creation'");
    }

    @Override
    public IAuthorizationBuilder creation(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'creation'");
    }

    @Override
    public IAuthorizationBuilder expiration(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'expiration'");
    }

    @Override
    public IAuthorizationBuilder expiration(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'expiration'");
    }

    @Override
    public IAuthorizationBuilder expiration(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'expiration'");
    }

    @Override
    public IAuthorizationBuilder revoked(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'revoked'");
    }

    @Override
    public IAuthorizationBuilder revoked(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'revoked'");
    }

    @Override
    public IAuthorizationBuilder revoked(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'revoked'");
    }

    @Override
    public IAuthorizationBuilder toByteArray(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toByteArray'");
    }

    @Override
    public IAuthorizationBuilder toByteArray(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toByteArray'");
    }

    @Override
    public IAuthorizationBuilder toByteArray(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toByteArray'");
    }

    @Override
    public IAuthorizationBuilder validate(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validate'");
    }

    @Override
    public IAuthorizationBuilder validate(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validate'");
    }

    @Override
    public IAuthorizationBuilder validate(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validate'");
    }

    @Override
    public IAuthorizationBuilder validateAgainst(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateAgainst'");
    }

    @Override
    public IAuthorizationBuilder validateAgainst(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateAgainst'");
    }

    @Override
    public IAuthorizationBuilder validateAgainst(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateAgainst'");
    }

    @Override
    public ISignableAuthorizationBuilder signable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'signable'");
    }

    @Override
    public IRefreshableAuthorizationBuilder refreshable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshable'");
    }

    @Override
    public IDomainBuilder up() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'up'");
    }

}
