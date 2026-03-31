package com.garganttua.api.core.builder;

import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.context.security.AuthorizationContext;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.security.context.IAuthorizationContext;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationMethodBinderBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;

public class AuthorizationBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IAuthorizationBuilder<E>, IDomainSecurityBuilder<E>, IAuthorizationContext>
        implements IAuthorizationBuilder<E> {

    private static final IReflectionProvider PROVIDER = new RuntimeReflectionProvider();

    private IClass<?> entityClass;
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

    public AuthorizationBuilder(IDomainSecurityBuilder<E> domainBuilder, IClass<?> entityClass) {
        super(domainBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IAuthorizationBuilder<E> type(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.type = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> type(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.type = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> type(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.type = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> authorities(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> authorities(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> authorities(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.authorities = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.expiration = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(Instant.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> expirable(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Instant.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> expirable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(Instant.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> revokable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> revokable(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public ISignableAuthorizationBuilder<E> signable() {
        if (this.signable == null) {
            this.signable = new SignableAuthorizationBuilder<>(this, this.entityClass);
        }
        return this.signable;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> refreshable() {
        if (this.refreshable == null) {
            this.refreshable = new RefreshableAuthorizationBuilder<>(this, this.entityClass);
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
