package com.garganttua.api.core.builder;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.ISignableAuthorizationMethodBinderBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;

public class SignableAuthorizationBuilder<E> extends AbstractAutomaticLinkedBuilder<ISignableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, Object> implements ISignableAuthorizationBuilder<E> {

    private @Nonnull IObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private ISignableAuthorizationMethodBinderBuilder<E> sign;

    public SignableAuthorizationBuilder(IAuthorizationBuilder<E> authorizationBuilder, IObjectQuery objectQuery,
            Class<?> entityClass) {
                super(authorizationBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    protected void doAutoDetection() {

    }

    @Override
    public ISignableAuthorizationBuilder<E> setSignature(String string) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> setSignature(Method method) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> setSignature(ObjectAddress fieldAddress) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> getSignature(String string) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> getSignature(Method method) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> getSignature(ObjectAddress fieldAddress) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> getDataToSign(String string) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> getDataToSign(Method method) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> getDataToSign(ObjectAddress fieldAddress) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    protected synchronized Object doBuild() throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doBuild'");
    }

}
