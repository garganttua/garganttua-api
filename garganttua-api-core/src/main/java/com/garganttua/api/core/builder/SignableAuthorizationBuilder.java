package com.garganttua.api.core.builder;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.ISignableAuthorizationBuilder;
import com.garganttua.api.spec.engine.ISignableAuthorizationMethodBinderBuilder;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.query.IGGObjectQuery;

public class SignableAuthorizationBuilder extends AbstractAutomaticLinkedBuilder<Object, ISignableAuthorizationBuilder, IAuthorizationBuilder> implements ISignableAuthorizationBuilder {

    private @Nonnull IGGObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private ISignableAuthorizationMethodBinderBuilder sign;

    public SignableAuthorizationBuilder(IAuthorizationBuilder authorizationBuilder, IGGObjectQuery objectQuery,
            Class<?> entityClass) {
                super(authorizationBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    protected Object doBuild() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doBuild'");
    }

    @Override
    protected void doAutoDetection() {
        
    }

    @Override
    public ISignableAuthorizationBuilder setSignature(String string) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder setSignature(Method method) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder setSignature(GGObjectAddress fieldAddress) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder getSignature(String string) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder getSignature(Method method) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder getSignature(GGObjectAddress fieldAddress) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignature'");
    }

    @Override
    public ISignableAuthorizationBuilder getDataToSign(String string) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    public ISignableAuthorizationBuilder getDataToSign(Method method) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

    @Override
    public ISignableAuthorizationBuilder getDataToSign(GGObjectAddress fieldAddress) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
    }

}
