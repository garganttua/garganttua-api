package com.garganttua.api.core.engine;

import java.util.Objects;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.Method;
import com.garganttua.api.spec.engine.Action;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IUseCaseBinderBuilder;
import com.garganttua.api.spec.engine.IUseCaseBuilder;
import com.garganttua.api.spec.service.ServiceAccess;

public class UseCaseBuilder implements IUseCaseBuilder {

    private Boolean autoDetect = false;

    public UseCaseBuilder(String useCaseName, IDomainBuilder domainBuilder) {
        //TODO Auto-generated constructor stub
    }

    @Override
    public IUseCaseBuilder pathSuffix(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pathSuffix'");
    }

    @Override
    public IUseCaseBuilder completePath(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'completePath'");
    }

    @Override
    public IUseCaseBuilder action(Action allentities) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'action'");
    }

    @Override
    public IUseCaseBuilder method(Method create) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'method'");
    }

    @Override
    public IUseCaseBuilder input(Class<Object> class1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'input'");
    }

    @Override
    public IUseCaseBuilder output(Class<Object> class1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'output'");
    }

    @Override
    public IUseCaseBuilder authority(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authority'");
    }

    @Override
    public IUseCaseBuilder access(ServiceAccess anonymous) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'access'");
    }

    @Override
    public IUseCaseBinderBuilder bind(IObjectSupplierBuilder<?> supplier) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bind'");
    }

    @Override
    public IUseCaseBinderBuilder bind(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bind'");
    }

    @Override
    public IDomainBuilder up() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'up'");
    }

    @Override
    public IUseCaseBuilder autoDetect(boolean b) {
        this.autoDetect = Objects.requireNonNull(b, "AutoDetect cannot be null");
        return this;
    }

    @Override
    public Object build() throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

}
