package com.garganttua.api.core.builder;

import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;
import java.time.Instant;
import java.util.Objects;

import com.garganttua.api.commons.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;

public class RefreshableAuthorizationBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IRefreshableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, E>
        implements IRefreshableAuthorizationBuilder<E> {

    private static final IReflectionProvider PROVIDER = new RuntimeReflectionProvider();

    private final IClass<?> entityClass;
    private ObjectAddress revoked;
    private ObjectAddress expiration;
    private ObjectAddress encodeMethod;
    private ObjectAddress decodeMethod;

    public RefreshableAuthorizationBuilder(IAuthorizationBuilder<E> authorizationBuilder,
            IClass<?> entityClass) {
        super(authorizationBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        this.expiration = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(Instant.class)).address();
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> expirable(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        this.expiration = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Instant.class)).address();
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> expirable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        this.expiration = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(Instant.class)).address();
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> revokable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        this.revoked = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> revokable(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        this.revoked = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        this.revoked = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> encode(IMethod method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        this.encodeMethod = new ObjectAddress(method.getName());
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> encode(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.encodeMethod = new ObjectAddress(methodName);
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> encode(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        this.encodeMethod = methodAddress;
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> decode(IMethod method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        this.decodeMethod = new ObjectAddress(method.getName());
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> decode(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.decodeMethod = new ObjectAddress(methodName);
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> decode(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        this.decodeMethod = methodAddress;
        return this;
    }

    ObjectAddress getExpiration() { return this.expiration; }
    ObjectAddress getRevoked() { return this.revoked; }
    ObjectAddress getEncodeMethod() { return this.encodeMethod; }
    ObjectAddress getDecodeMethod() { return this.decodeMethod; }

    @Override
    protected synchronized E doBuild() {
        // The refreshable data is collected by the parent AuthorizationBuilder
        // via getExpiration() / getRevoked()
        return null;
    }

    @Override
    protected void doAutoDetection() {
    }
}
