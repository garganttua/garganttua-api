package com.garganttua.api.core.builder;

import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.authorization;
import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.key;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.garganttua.api.core.builder.binder.RefreshableAuthorizationMethodBinderBuilder;
import com.garganttua.api.core.builder.resolver.FieldResolver;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.engine.IRefreshableAuthorizationMethodBinderBuilder;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.query.IGGObjectQuery;

public class RefreshableAuthorizationBuilder
        extends AbstractAutomaticLinkedBuilder<Object, IRefreshableAuthorizationBuilder, IAuthorizationBuilder>
        implements IRefreshableAuthorizationBuilder {

    private @Nonnull IGGObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private GGObjectAddress revoked;
    private GGObjectAddress expiration;
    private GGObjectAddress authorities;
    private IRefreshableAuthorizationMethodBinderBuilder toByteArray;
    private IRefreshableAuthorizationMethodBinderBuilder validate;
    private IRefreshableAuthorizationMethodBinderBuilder validateAgainst;

    public RefreshableAuthorizationBuilder(IAuthorizationBuilder authorizationBuilder, IGGObjectQuery objectQuery,
            Class<?> entityClass) {
        super(authorizationBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IRefreshableAuthorizationBuilder expirable(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.expiration = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder expirable(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByField(field, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder expirable(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder revokable(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.revoked = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder revokable(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.revoked = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder revokable(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.revoked = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder encode(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization(this.entityClass))
                .method(method, Byte[].class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder encode(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization(this.entityClass))
                .method(methodName, Byte[].class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder encode(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization(this.entityClass))
                .method(methodAddress, Byte[].class);

        return this;
    }

    @Override
    protected Object doBuild() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doBuild'");
    }

    @Override
    protected void doAutoDetection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

    @Override
    public IRefreshableAuthorizationBuilder decode(Method method) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

    @Override
    public IRefreshableAuthorizationBuilder decode(String methodName) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

    @Override
    public IRefreshableAuthorizationBuilder decode(GGObjectAddress fieldAddress) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

}
