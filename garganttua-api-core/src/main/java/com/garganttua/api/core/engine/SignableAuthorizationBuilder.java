package com.garganttua.api.core.engine;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.ISignableAuthorizationBuilder;
import com.garganttua.api.spec.engine.ISignableAuthorizationMethodBinderBuilder;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.query.IGGObjectQuery;
import static com.garganttua.api.core.engine.ExecutionContext.Suppliers.*;

public class SignableAuthorizationBuilder implements ISignableAuthorizationBuilder {

    private @Nonnull IAuthorizationBuilder authorizationBuilder;
    private @Nonnull IGGObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private @Nonnull IDomainBuilder key;
    private ISignableAuthorizationMethodBinderBuilder sign;
    private Boolean autoDetect = false;

    public SignableAuthorizationBuilder(IAuthorizationBuilder authorizationBuilder, IGGObjectQuery objectQuery,
            Class<?> entityClass, IDomainBuilder key) {
        this.authorizationBuilder = Objects.requireNonNull(authorizationBuilder,
                "Authorization builder cannot be null");
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        this.key = Objects.requireNonNull(key, "Key cannot be null");
    }

    @Override
    public ISignableAuthorizationBuilder sign(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.sign = new SignableAuthorizationMethodBinderBuilder(this, authorization())
                .method(methodName, null, this.key.getEntityClass())
                .withParam(0, key());

        return this;
    }

    @Override
    public ISignableAuthorizationBuilder sign(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.sign = new SignableAuthorizationMethodBinderBuilder(this, authorization())
                .method(method, null, this.key.getEntityClass())
                .withParam(0, key());

        return this;
    }

    @Override
    public ISignableAuthorizationBuilder sign(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.sign = new SignableAuthorizationMethodBinderBuilder(this, authorization())
                .method(methodAddress, null, this.key.getEntityClass())
                .withParam(0, key());

        return this;
    }

    @Override
    public IAuthorizationBuilder up() {
        return this.authorizationBuilder;
    }

    @Override
    public Object build() throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

    @Override
    public ISignableAuthorizationBuilder autoDetect(boolean b) {
        this.autoDetect = Objects.requireNonNull(b, "AutoDetect cannot be null");
        return this;
    }

}
