package com.garganttua.api.core.builder;

import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.builder.binder.AuthorizationMethodBinderBuilder;
import com.garganttua.api.core.context.application.AuthorizationContext;
import com.garganttua.api.spec.context.IAuthorizationContext;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationMethodBinderBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;

public class AuthorizationBuilder
        extends AbstractAutomaticLinkedBuilder<IAuthorizationBuilder, IDomainSecurityBuilder, IAuthorizationContext>
        implements IAuthorizationBuilder {

    private IObjectQuery objectQuery;
    private Class<?> entityClass;
    private ObjectAddress type;
    private ObjectAddress revoked;
    private ObjectAddress creation;
    private ObjectAddress expiration;
    private ObjectAddress authorities;
    private IAuthorizationMethodBinderBuilder toByteArray;
    private IAuthorizationMethodBinderBuilder validate;
    private IAuthorizationMethodBinderBuilder validateAgainst;
    private IAuthorizationMethodBinderBuilder fromByteArray;
    private ISignableAuthorizationBuilder signable;
    private IRefreshableAuthorizationBuilder refreshable;
    private boolean storable = false;

    public AuthorizationBuilder(IDomainSecurityBuilder domainBuilder, IObjectQuery objectQuery, Class<?> entityClass) {
        super(domainBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IAuthorizationBuilder type(Field field) throws DslException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.type = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder type(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.type = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder type(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.type = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder authorities(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder authorities(Field field) throws DslException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByField(field, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder authorities(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.authorities = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder expirable(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.expiration = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder expirable(Field field) throws DslException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByField(field, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder expirable(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder revokable(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder revokable(Field field) throws DslException {
        Objects.requireNonNull(field, "Field name cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder revokable(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder encode(
            Method method) throws DslException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.toByteArray = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                .method(method, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder encode(
            String methodName) throws DslException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.toByteArray = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                .method(methodName, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder encode(
            ObjectAddress methodAddress) throws DslException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.toByteArray = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                .method(methodAddress, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder decode(Method method) throws DslException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.fromByteArray = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                .method(method, void.class, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder decode(String methodName) throws DslException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.fromByteArray = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                .method(methodName, void.class, Byte[].class);

        return this;
    }

    @Override
    public IAuthorizationBuilder decode(ObjectAddress methodAddress) throws DslException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.fromByteArray = new AuthorizationMethodBinderBuilder(this, authorization(this.entityClass))
                .method(methodAddress, void.class, Byte[].class);

        return this;
    }

   /*  @Override
    public IAuthorizationBuilder validate(
            String methodName) throws DslException {
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
            Method method) throws DslException {
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
            ObjectAddress methodAddress) throws DslException {
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
            String methodName) throws DslException {
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
            Method method) throws DslException {
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
            ObjectAddress methodAddress) throws DslException {
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
    public ISignableAuthorizationBuilder signable() {
        if (this.signable == null) {
            this.signable = new SignableAuthorizationBuilder(this, this.objectQuery, this.entityClass);
        }
        return this.signable;
    }

    @Override
    public IRefreshableAuthorizationBuilder refreshable() {
        if (this.refreshable == null) {
            this.refreshable = new RefreshableAuthorizationBuilder(this, this.objectQuery, this.entityClass);
        }
        return this.refreshable;
    }

    @Override
    public IAuthorizationBuilder storable(boolean b) {
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
    protected IAuthorizationContext doBuild() throws DslException {
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
    protected void doAutoDetection() throws DslException {

    }

}
