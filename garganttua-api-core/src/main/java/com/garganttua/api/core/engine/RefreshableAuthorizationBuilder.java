package com.garganttua.api.core.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IRefreshableAuthorizationBuilder;
import com.garganttua.api.spec.engine.IRefreshableAuthorizationMethodBinderBuilder;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.query.IGGObjectQuery;
import static com.garganttua.api.core.engine.ExecutionContext.Suppliers.*;

public class RefreshableAuthorizationBuilder implements IRefreshableAuthorizationBuilder {

    private @Nonnull IAuthorizationBuilder authorizationBuilder;
    private @Nonnull IGGObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private Optional<IDomainBuilder> key;
    private Field revoked;
    private Field expiration;
    private Field authorities;
    private IRefreshableAuthorizationMethodBinderBuilder toByteArray;
    private IRefreshableAuthorizationMethodBinderBuilder validate;
    private IRefreshableAuthorizationMethodBinderBuilder validateAgainst;
    private Boolean autoDetect = false;

    public RefreshableAuthorizationBuilder(IAuthorizationBuilder authorizationBuilder, IGGObjectQuery objectQuery,
            Class<?> entityClass, Optional<IDomainBuilder> key) {
        this.authorizationBuilder = Objects.requireNonNull(authorizationBuilder,
                "Authorization builder cannot be null");
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        this.key = Objects.requireNonNull(key, "Signable cannot be null");
    }

    @Override
    public IRefreshableAuthorizationBuilder expiration(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.expiration = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder expiration(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByField(field, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder expiration(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Instant.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder revoked(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.revoked = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder revoked(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.revoked = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder revoked(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.revoked = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder toByteArray(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization())
                .method(method, Byte[].class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder toByteArray(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization())
                .method(methodName, Byte[].class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder toByteArray(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.toByteArray = new RefreshableAuthorizationMethodBinderBuilder(this,
                authorization())
                .method(methodAddress, Byte[].class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder validate(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        if (this.key.isPresent())
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodName, Boolean.class, this.key.get().getEntityClass())
                    .withParam(0, key());
        else
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodName, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder validate(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        if (this.key.isPresent())
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(method, Boolean.class, this.key.get().getEntityClass())
                    .withParam(0, key());
        else
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(method, Boolean.class);
        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder validate(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        if (this.key.isPresent())
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodAddress, Boolean.class, this.key.get().getEntityClass())
                    .withParam(0, key());
        else
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodAddress, Boolean.class);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder validateAgainst(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        if (this.key.isPresent())
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodName, Boolean.class, this.entityClass, this.key.get().getEntityClass())
                    .withParam(0, authorization())
                    .withParam(1, key());
        else
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodName, Boolean.class, this.entityClass);

        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder validateAgainst(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        if (this.key.isPresent())
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(method, Boolean.class, this.entityClass, this.key.get().getEntityClass())
                    .withParam(0, authorization())
                    .withParam(1, key());
        else
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(method, Boolean.class, this.entityClass);


        return this;
    }

    @Override
    public IRefreshableAuthorizationBuilder validateAgainst(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        if (this.key.isPresent())
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodAddress, Boolean.class, this.entityClass, this.key.get().getEntityClass())
                    .withParam(0, authorization())
                    .withParam(1, key());
        else
            this.validate = new RefreshableAuthorizationMethodBinderBuilder(this,
                    authorization())
                    .method(methodAddress, Boolean.class, this.entityClass);


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
    public IRefreshableAuthorizationBuilder autoDetect(boolean b) {
        this.autoDetect = Objects.requireNonNull(b, "AutoDetect cannot be null");
        return this;
    }
}
