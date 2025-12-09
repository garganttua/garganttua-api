package com.garganttua.api.core.builder;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.ISignableAuthorizationMethodBinderBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;

public class SignableAuthorizationBuilder extends AbstractAutomaticLinkedBuilder<ISignableAuthorizationBuilder, IAuthorizationBuilder, Object> implements ISignableAuthorizationBuilder {

    private @Nonnull IObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private ISignableAuthorizationMethodBinderBuilder sign;

    public SignableAuthorizationBuilder(IAuthorizationBuilder authorizationBuilder, IObjectQuery objectQuery,
            Class<?> entityClass) {
                super(authorizationBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    protected void doAutoDetection() {
        
    }

    @Override
    public ISignableAuthorizationBuilder setSignature(String string) throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder setSignature(Method method) throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder setSignature(ObjectAddress fieldAddress) throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder getSignature(String string) throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder getSignature(Method method) throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder getSignature(ObjectAddress fieldAddress) throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder getDataToSign(String string) throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    public ISignableAuthorizationBuilder getDataToSign(Method method) throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    public ISignableAuthorizationBuilder getDataToSign(ObjectAddress fieldAddress) throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    protected Object doBuild() throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doBuild'");
    }

}
