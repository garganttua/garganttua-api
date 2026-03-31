package com.garganttua.api.core.builder;

import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;
import java.util.Objects;

import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public class SignableAuthorizationBuilder<E> extends AbstractAutomaticLinkedBuilder<ISignableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, Object> implements ISignableAuthorizationBuilder<E> {

    private IClass<?> entityClass;

    public SignableAuthorizationBuilder(IAuthorizationBuilder<E> authorizationBuilder,
            IClass<?> entityClass) {
                super(authorizationBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    protected void doAutoDetection() {
    }

    @Override
    public ISignableAuthorizationBuilder<E> signature(String string) throws ApiException {
        throw new UnsupportedOperationException("Unimplemented method 'signature'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> signature(IField field) throws ApiException {
        throw new UnsupportedOperationException("Unimplemented method 'signature'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> signature(ObjectAddress fieldAddress) throws ApiException {
        throw new UnsupportedOperationException("Unimplemented method 'signature'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> getDataToSign(String string) throws ApiException {
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> getDataToSign(IMethod method) throws ApiException {
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    public ISignableAuthorizationBuilder<E> getDataToSign(ObjectAddress fieldAddress) throws ApiException {
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    protected synchronized Object doBuild() throws ApiException {
        throw new UnsupportedOperationException("Unimplemented method 'doBuild'");
    }
}
