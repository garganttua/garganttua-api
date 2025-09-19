package com.garganttua.api.core.engine;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.spec.engine.IAuthenticatorBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.query.IGGObjectQuery;

public class AuthenticatorBuilder implements IAuthenticatorBuilder {

    private @Nonnull IDomainBuilder domainBuilder;
    private @Nonnull IGGObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private boolean alwaysEnabled = false;
    private boolean autoDetect = false;
    private Field login;
    private Field authorities;
    private Field credentialsNonExpired;
    private Field enabled;
    private Field accountNonLocked;
    private Field accountNonExpired;
    private AuthenticatorScope scope;

    public AuthenticatorBuilder(IDomainBuilder domainBuilder, IGGObjectQuery objectQuery, Class<?> entityClass) {
        this.domainBuilder = Objects.requireNonNull(domainBuilder, "Domain builder cannot be null");
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IAuthenticatorBuilder autoDetect(boolean b) {
        this.autoDetect = Objects.requireNonNull(b, "AutoDetect cannot be null");
        return this;
    }

    @Override
    public IAuthenticatorBuilder login(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.login = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder login(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.login = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder login(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.login = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByField(field, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.authorities = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder alwaysEnabled(boolean b) {
        this.alwaysEnabled = true;
        return this;
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.credentialsNonExpired = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.credentialsNonExpired = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.credentialsNonExpired = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.enabled = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.enabled = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.enabled = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.accountNonExpired = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.accountNonExpired = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.accountNonExpired = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.accountNonLocked = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.accountNonLocked = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.accountNonLocked = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder scope(AuthenticatorScope scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public IAuthenticatorBuilder authentication(Class<?> class1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authentication'");
    }

    @Override
    public IDomainBuilder up() {
        return this.domainBuilder;
    }

    @Override
    public IAuthenticatorAuthorizationBuilder authorization(IDomainBuilder authorization) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorization'");
    }

    @Override
    public Object build() throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

}
