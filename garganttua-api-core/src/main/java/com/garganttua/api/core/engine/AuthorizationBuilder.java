package com.garganttua.api.core.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.core.builder.AuthorizationMethodBinderBuilder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IAuthorizationMethodBinderBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.engine.ISignableAuthorizationBuilder;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.query.IGGObjectQuery;
import static com.garganttua.api.core.engine.ExecutionContext.Suppliers.*;

public class AuthorizationBuilder implements IAuthorizationBuilder {

    private IDomainBuilder domainBuilder;
    private boolean autoDetect = false;
    private IGGObjectQuery objectQuery;
    private Class<?> entityClass;
    private Field type;
    private Field revoked;
    private Field creation;
    private Field expiration;
    private Field authorities;
    private IAuthorizationMethodBinderBuilder toByteArray;
    private IAuthorizationMethodBinderBuilder validate;
    private IAuthorizationMethodBinderBuilder validateAgainst;
    private IAuthorizationMethodBinderBuilder fromByteArray;
    private ISignableAuthorizationBuilder signable;
    private IRefreshableAuthorizationBuilder refreshable;
    private boolean storable = false;
    private IDomainBuilder key;

    public AuthorizationBuilder(IDomainBuilder domainBuilder, IGGObjectQuery objectQuery, Class<?> entityClass) {
        this.domainBuilder = Objects.requireNonNull(domainBuilder, "Domain builder cannot be null");
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IAuthorizationBuilder autoDetect(boolean b) {
        this.autoDetect = Objects.requireNonNull(b, "AutoDetect cannot be null");
        return this;
    }

    @Override
    public IAuthorizationBuilder type(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.type = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder type(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.type = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder type(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.type = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder authorities(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder authorities(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByField(field, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder authorities(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.authorities = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder creation(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.creation = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder creation(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.creation = FieldResolver.fieldByField(field, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder creation(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.creation = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder expiration(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.expiration = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder expiration(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByField(field, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder expiration(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder revoked(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder revoked(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder revoked(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder toByteArray(
            Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.toByteArray = new AuthorizationMethodBinderBuilder(this, authorization())
                .method(method, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder toByteArray(
            String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.toByteArray = new AuthorizationMethodBinderBuilder(this, authorization())
                .method(methodName, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder toByteArray(
            GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.toByteArray = new AuthorizationMethodBinderBuilder(this, authorization())
                .method(methodAddress, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder fromByteArray(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.fromByteArray = new AuthorizationMethodBinderBuilder(this, authorization())
                .method(method, void.class, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder fromByteArray(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.fromByteArray = new AuthorizationMethodBinderBuilder(this, authorization())
                .method(methodName, void.class, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder fromByteArray(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.fromByteArray = new AuthorizationMethodBinderBuilder(this, authorization())
                .method(methodAddress, void.class, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder validate(
            String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        if (this.signable != null)
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization())
                    .method(methodName, Boolean.class, this.key.getEntityClass())
                    .withParam(0, key());
        else
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization())
                    .method(methodName, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder validate(
            Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        if (this.signable != null)
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization())
                    .method(method, Boolean.class, this.key.getEntityClass())
                    .withParam(0, key());
        else
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization())
                    .method(method, Boolean.class);

        return this;
    }

    @Override
    public AuthorizationBuilder validate(
            GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        if (this.signable != null)
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization())
                    .method(methodAddress, Boolean.class, this.key.getEntityClass())
                    .withParam(0, key());
        else
            this.validate = new AuthorizationMethodBinderBuilder(this, authorization())
                    .method(methodAddress, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder validateAgainst(
            String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.storable = true;

        if (this.signable != null)
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodName, Boolean.class, this.entityClass, this.key.getEntityClass())
                    .withParam(0, authorization())
                    .withParam(1, key());
        else
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodName, Boolean.class, this.entityClass)
                    .withParam(0, authorization());

        return this;
    }

    @Override
    public IAuthorizationBuilder validateAgainst(
            Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.storable = true;

        if (this.signable != null)
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(method, Boolean.class, this.entityClass, this.key.getEntityClass())
                    .withParam(0, authorization())
                    .withParam(1, key());
        else
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(method, Boolean.class, this.entityClass)
                    .withParam(0, authorization());

        return this;
    }

    @Override
    public IAuthorizationBuilder validateAgainst(
            GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.storable = true;

        if (this.signable != null)
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodAddress, Boolean.class, this.entityClass, this.key.getEntityClass())
                    .withParam(0, authorization())
                    .withParam(1, key());
        else
            this.validateAgainst = new AuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodAddress, Boolean.class, this.entityClass)
                    .withParam(0, authorization());

        return this;
    }

    @Override
    public ISignableAuthorizationBuilder signable(IDomainBuilder key) {
        Objects.requireNonNull(key, "Key cannot be null");
        if (this.signable == null) {
            this.key = key;
            this.signable = new SignableAuthorizationBuilder(this, this.objectQuery, this.entityClass, key);
        }
        return this.signable;
    }

    @Override
    public IRefreshableAuthorizationBuilder refreshable() {
        if (this.refreshable == null) {
            this.refreshable = new RefreshableAuthorizationBuilder(this, this.objectQuery, this.entityClass,
                    Optional.ofNullable(this.key));
        }
        return this.refreshable;
    }

    @Override
    public IDomainBuilder up() {
        return this.domainBuilder;
    }

    @Override
    public IAuthorizationBuilder storable(boolean b) {
        this.storable = b;
        return this;
    }

    @Override
    public Object build() throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

}
