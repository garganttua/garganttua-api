package com.garganttua.api.core.engine;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.security.IDomainSecurityAuthorizationBuilder;
import com.garganttua.api.spec.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.security.ISecurityEngine;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;

public class DomainSecurityBuilder implements IDomainSecurityBuilder {

    @Override
    public IDomainBuilder up() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'up'");
    }

    @Override
    public IDomainSecurityBuilder autoDetect(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'autoDetect'");
    }

    @Override
    public IDomainSecurityBuilder injector(IGGInjector injector) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'injector'");
    }

    @Override
    public ISecurityEngine build() throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

    @Override
    public IDomainSecurityBuilder loader(IGGBeanLoader loader) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loader'");
    }

    @Override
    public IDomainSecurityBuilder engine(IEngine engine) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'engine'");
    }

    @Override
    public IDomainSecurityBuilder creationAccess(ServiceAccess access) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'creationAccess'");
    }

    @Override
    public IDomainSecurityBuilder readAllAccess(ServiceAccess tenant) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readAllAccess'");
    }

    @Override
    public IDomainSecurityBuilder readOneAccess(ServiceAccess tenant) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readOneAccess'");
    }

    @Override
    public IDomainSecurityBuilder updateAccess(ServiceAccess tenant) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAccess'");
    }

    @Override
    public IDomainSecurityBuilder deleteAllAccess(ServiceAccess tenant) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllAccess'");
    }

    @Override
    public IDomainSecurityBuilder deleteOneAccess(ServiceAccess tenant) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteOneAccess'");
    }

    @Override
    public IDomainSecurityBuilder creationAuthority(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'creationAuthority'");
    }

    @Override
    public IDomainSecurityBuilder readAllAuthority(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readAllAuthority'");
    }

    @Override
    public IDomainSecurityBuilder readOneAuthority(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readOneAuthority'");
    }

    @Override
    public IDomainSecurityBuilder updateAuthority(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAuthority'");
    }

    @Override
    public IDomainSecurityBuilder deleteAllAuthority(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllAuthority'");
    }

    @Override
    public IDomainSecurityBuilder deleteOneAuthority(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteOneAuthority'");
    }

    @Override
    public IDomainSecurityAuthorizationBuilder authorization(IDomainBuilder authorizationDomain) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorization'");
    }

}
