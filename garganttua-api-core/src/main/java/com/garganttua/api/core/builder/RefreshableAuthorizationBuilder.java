package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationMethodBinderBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;

public class RefreshableAuthorizationBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IRefreshableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, E>
        implements IRefreshableAuthorizationBuilder<E> {

    private @Nonnull IObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private ObjectAddress revoked;
    private ObjectAddress expiration;
    private ObjectAddress authorities;
    private IRefreshableAuthorizationMethodBinderBuilder<E> toByteArray;
    private IRefreshableAuthorizationMethodBinderBuilder<E> validate;
    private IRefreshableAuthorizationMethodBinderBuilder<E> validateAgainst;

    public RefreshableAuthorizationBuilder(IAuthorizationBuilder<E> authorizationBuilder, IObjectQuery objectQuery,
            Class<?> entityClass) {
        super(authorizationBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.expiration = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> expirable(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByField(field, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> expirable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> revokable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.revoked = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> revokable(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.revoked = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.revoked = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> encode(Method method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");

       /*  this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization(this.entityClass))
                .method(method, Byte[].class); */

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> encode(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        /* this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization(this.entityClass))
                .method(methodName, Byte[].class); */

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> encode(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        /* this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization(this.entityClass))
                .method(methodAddress, Byte[].class);
 */
        return this;
    }

    @Override
    protected synchronized E doBuild() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doBuild'");
    }

    @Override
    protected void doAutoDetection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> decode(Method method) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> decode(String methodName) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> decode(ObjectAddress fieldAddress) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

}
