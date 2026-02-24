package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.garganttua.api.core.context.application.AuthenticatorContext;
import com.garganttua.api.spec.context.IAuthenticatorContext;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;

public class AuthenticatorBuilder<E> extends AbstractAutomaticLinkedBuilder<IAuthenticatorBuilder<E>, IDomainSecurityBuilder<E>, IAuthenticatorContext>
        implements IAuthenticatorBuilder<E> {

    private @Nonnull IObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private boolean alwaysEnabled = false;
    private ObjectAddress login;
    private ObjectAddress authorities;
    private ObjectAddress credentialsNonExpired;
    private ObjectAddress enabled;
    private ObjectAddress accountNonLocked;
    private ObjectAddress accountNonExpired;
    private AuthenticatorScope scope;
    private List<IAuthenticationBuilder> selectedAuthentications = new ArrayList<>();
    private IAuthenticatorAuthorizationBuilder<E> authenticatorAuthorizationBuilder;

    public AuthenticatorBuilder(IDomainSecurityBuilder<E> domainBuilder, IObjectQuery objectQuery, Class<?> entityClass) {
        super(domainBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IAuthenticatorBuilder login(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.login = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder login(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.login = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder login(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.login = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByField(field, this.entityClass, List.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(ObjectAddress fieldAddress) throws ApiException {
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
    public IAuthenticatorBuilder credentialsNonExpired(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.credentialsNonExpired = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.credentialsNonExpired = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.credentialsNonExpired = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.enabled = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.enabled = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.enabled = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.accountNonExpired = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.accountNonExpired = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.accountNonExpired = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(Field field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.accountNonLocked = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.accountNonLocked = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass,
                Boolean.class);

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(ObjectAddress fieldAddress) throws ApiException {
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
    public IAuthenticatorBuilder authentication(IAuthenticationBuilder authentication) throws ApiException {
        Objects.requireNonNull(authentication, "Authentication cannot be null");
        this.selectedAuthentications.add(authentication);
        return this;
    }

    @Override
    public IAuthenticatorAuthorizationBuilder authorization(IDomainBuilder authorization) {
        Objects.requireNonNull(authorization, "Authentication cannot be null");

        if (this.authenticatorAuthorizationBuilder == null) {
            this.authenticatorAuthorizationBuilder = new AuthenticatorAuthorizationBuilder<>(this);
        }

        return this.authenticatorAuthorizationBuilder;
    }

    @Override
    protected synchronized IAuthenticatorContext doBuild() throws ApiException {
        return new AuthenticatorContext(
                this.alwaysEnabled,
                this.login,
                this.authorities,
                this.credentialsNonExpired,
                this.enabled,
                this.accountNonLocked,
                this.accountNonExpired,
                this.scope,
                this.selectedAuthentications.stream().map(builder -> {
                    try {
                        return builder.build();
                    } catch (ApiException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList()),
                this.authenticatorAuthorizationBuilder != null ? this.authenticatorAuthorizationBuilder.build() : null);
    }

    @Override
    protected void doAutoDetection() throws ApiException {

    }

}
