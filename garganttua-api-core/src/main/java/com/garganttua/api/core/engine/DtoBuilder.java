package com.garganttua.api.core.engine;

import java.lang.reflect.Field;
import java.util.Objects;

import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IDtoBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.reflection.GGObjectAddress;

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
        this.autoDetect = b;
        return this;
    }

    @Override
    public IDtoBuilder tenantId(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tenantId'");
    }

    @Override
    public IDtoBuilder tenantId(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tenantId'");
    }

    @Override
    public IDtoBuilder tenantId(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tenantId'");
    }

    @Override
    public IDomainBuilder up() {
        return this.domainBuilder;
    }

    @Override
    public IDtoBuilder db(IObjectSupplier<?> daoSupplier) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'db'");
    }

    @Override
    public IDtoBuilder db(IDao dao) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'db'");
    }

}
