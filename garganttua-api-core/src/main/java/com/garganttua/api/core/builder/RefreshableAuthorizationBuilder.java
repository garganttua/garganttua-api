package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationMethodBinderBuilder;
import com.garganttua.core.CoreException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;

public class RefreshableAuthorizationBuilder
        extends AbstractAutomaticLinkedBuilder<IRefreshableAuthorizationBuilder, IAuthorizationBuilder, Object>
        implements IRefreshableAuthorizationBuilder {

    private @Nonnull IObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private ObjectAddress revoked;
    private ObjectAddress expiration;
    private ObjectAddress authorities;
    private IRefreshableAuthorizationMethodBinderBuilder toByteArray;
    private IRefreshableAuthorizationMethodBinderBuilder validate;
    private IRefreshableAuthorizationMethodBinderBuilder validateAgainst;

    public RefreshableAuthorizationBuilder(IAuthorizationBuilder authorizationBuilder, IObjectQuery objectQuery,
            Class<?> entityClass) {
        super(authorizationBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IRefreshableAuthorizationBuilder expirable(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.expiration = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder expirable(Field field) throws DslException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByField(field, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder expirable(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder revokable(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.revoked = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder revokable(Field field) throws DslException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.revoked = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder revokable(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.revoked = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder encode(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

       /*  this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization(this.entityClass))
                .method(method, Byte[].class); */

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder encode(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        /* this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization(this.entityClass))
                .method(methodName, Byte[].class); */

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder encode(ObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        /* this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization(this.entityClass))
                .method(methodAddress, Byte[].class);
 */
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
    public IRefreshableAuthorizationBuilder decode(ObjectAddress fieldAddress) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

}
