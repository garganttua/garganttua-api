package com.garganttua.api.core.engine;

import java.util.Objects;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IDtoBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class DtoBuilder implements IDtoBuilder {

    private Class<?> dtoClass;
    private IDomainBuilder domainBuilder;
    private boolean autoDetect = false;

    public DtoBuilder(Class<?> dtoClass, IDomainBuilder domainBuilder) {
        this.dtoClass = Objects.requireNonNull(dtoClass, "Dto class cannot be null");
        this.domainBuilder = Objects.requireNonNull(domainBuilder, "Domain builder cannot be null");
    }

    @Override
    public IDtoBuilder autoDetect(boolean b) {
        this.autoDetect = Objects.requireNonNull(b, "AutoDetect cannot be null");
        return this;
    }

    @Override
    public IDomainBuilder up() {
        return this.domainBuilder;
    }

    @Override
    public IDtoBuilder db(IObjectSupplierBuilder<?> daoSupplier) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'db'");
    }

    @Override
    public IDtoBuilder db(IDao dao) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'db'");
    }

    @Override
    public Object build() throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

}
