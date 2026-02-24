package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.context.application.AuthorizationContext;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.IAuthorizationContext;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationMethodBinderBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;

public class AuthorizationBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IAuthorizationBuilder<E>, IDomainSecurityBuilder<E>, IAuthorizationContext>
        implements IAuthorizationBuilder<E> {

    private IObjectQuery objectQuery;
    private Class<?> entityClass;
    private ObjectAddress type;
    private ObjectAddress revoked;
    private ObjectAddress creation;
    private ObjectAddress expiration;
    private ObjectAddress authorities;
    private IAuthorizationMethodBinderBuilder<E> toByteArray;
    private IAuthorizationMethodBinderBuilder<E> validate;
    private IAuthorizationMethodBinderBuilder<E> validateAgainst;
    private IAuthorizationMethodBinderBuilder<E> fromByteArray;
    private ISignableAuthorizationBuilder<E> signable;
    private IRefreshableAuthorizationBuilder<E> refreshable;
    private boolean storable = false;

    public AuthorizationBuilder(IDomainSecurityBuilder<E> domainBuilder, IObjectQuery objectQuery, Class<?> entityClass) {
        super(domainBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IAuthorizationBuilder<E> type(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.type = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> type(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.type = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> type(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.type = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> authorities(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> authorities(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByField(field, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> authorities(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.authorities = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.expiration = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> expirable(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByField(field, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> expirable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> revokable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> revokable(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationMethodBinderBuilder<E> encode(
            Method method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        // TODO: Implement when ISupplierBuilder for authorization context is available
        throw new UnsupportedOperationException("Unimplemented method 'encode'");
    }

    @Override
    public IAuthorizationMethodBinderBuilder<E> encode(
            String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        // TODO: Implement when ISupplierBuilder for authorization context is available
        throw new UnsupportedOperationException("Unimplemented method 'encode'");
    }

    @Override
    public IAuthorizationMethodBinderBuilder<E> encode(
            ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        // TODO: Implement when ISupplierBuilder for authorization context is available
        throw new UnsupportedOperationException("Unimplemented method 'encode'");
    }

    @Override
    public IAuthorizationMethodBinderBuilder<E> decode(Method method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        // TODO: Implement when ISupplierBuilder for authorization context is available
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

    @Override
    public IAuthorizationMethodBinderBuilder<E> decode(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        // TODO: Implement when ISupplierBuilder for authorization context is available
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

    @Override
    public IAuthorizationMethodBinderBuilder<E> decode(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        // TODO: Implement when ISupplierBuilder for authorization context is available
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

   /*  @Override
    public IAuthorizationBuilder validate(
            String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        if (this.signable != null)
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                    .method(methodName, Boolean.class, this.key.getEntityClass())
                    .withParam(0, key(this.key.getEntityClass()));
        else
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                    .method(methodName, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder validate(
            Method method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");

        if (this.signable != null)
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                    .method(method, Boolean.class, this.key.getEntityClass())
                    .withParam(0, key(this.key.getEntityClass()));
        else
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                    .method(method, Boolean.class);

        return this;
    }

    @Override
    public AuthorizationBuilder validate(
            ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        if (this.signable != null)
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                    .method(methodAddress, Boolean.class, this.key.getEntityClass())
                    .withParam(0, key(this.key.getEntityClass()));
        else
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                    .method(methodAddress, Boolean.class);

        return this;
    } */

    /* @Override
    public IAuthorizationBuilder validateAgainst(
            String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.storable = true;

        if (this.signable != null)
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization(this.entityClass))
                    .method(methodName, Boolean.class, this.entityClass, this.key.getEntityClass())
                    .withParam(0, authorization(this.entityClass))
                    .withParam(1, key(this.key.getEntityClass()));
        else
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization(this.entityClass))
                    .method(methodName, Boolean.class, this.entityClass)
                    .withParam(0, authorization(this.entityClass));

        return this;
    }

    @Override
    public IAuthorizationBuilder validateAgainst(
            Method method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.storable = true;

        if (this.signable != null)
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization(this.entityClass))
                    .method(method, Boolean.class, this.entityClass, this.key.getEntityClass())
                    .withParam(0, authorization(this.entityClass))
                    .withParam(1, key(this.key.getEntityClass()));
        else
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization(this.entityClass))
                    .method(method, Boolean.class, this.entityClass)
                    .withParam(0, authorization(this.entityClass));

        return this;
    }

    @Override
    public IAuthorizationBuilder validateAgainst(
            ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.storable = true;

        if (this.signable != null)
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization(this.entityClass))
                    .method(methodAddress, Boolean.class, this.entityClass, this.key.getEntityClass())
                    .withParam(0, authorization(this.entityClass))
                    .withParam(1, key(this.key.getEntityClass()));
        else
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization(this.entityClass))
                    .method(methodAddress, Boolean.class, this.entityClass)
                    .withParam(0, authorization(this.entityClass));

        return this;
    } */

    @Override
    public ISignableAuthorizationBuilder<E> signable() {
        if (this.signable == null) {
            this.signable = new SignableAuthorizationBuilder<>(this, this.objectQuery, this.entityClass);
        }
        return this.signable;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> refreshable() {
        if (this.refreshable == null) {
            this.refreshable = new RefreshableAuthorizationBuilder<>(this, this.objectQuery, this.entityClass);
        }
        return this.refreshable;
    }

    @Override
    public IAuthorizationBuilder<E> storable(boolean b) {
        this.storable = b;
        return this;
    }

    @Override
    public Boolean isStorable() {
        return this.storable;
    }

    @Override
    public Boolean isRefreshable() {
        return this.refreshable != null;
    }

    @Override
    protected synchronized IAuthorizationContext doBuild() throws ApiException {
        return new AuthorizationContext(
                this.type,
                this.revoked,
                this.creation,
                this.expiration,
                this.authorities,
                this.toByteArray,
                this.validate,
                this.validateAgainst,
                this.fromByteArray,
                this.signable.build(),
                this.refreshable.build(),
                this.storable);
    }

    @Override
    protected void doAutoDetection() throws ApiException {

    }

}
