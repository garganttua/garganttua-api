package com.garganttua.api.core.builder;

import com.garganttua.core.reflection.IField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.context.security.AuthenticatorContext;
import com.garganttua.api.core.definition.AuthenticatorDefintion;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.definition.IAuthenticationDefinition;
import com.garganttua.api.spec.definition.IAuthenticatorDefinition;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.security.context.IAuthenticatorContext;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;

public class AuthenticatorBuilder<E> extends
        AbstractAutomaticLinkedBuilder<IAuthenticatorBuilder<E>, IDomainSecurityBuilder<E>, IAuthenticatorContext>
        implements IAuthenticatorBuilder<E> {

    private static final IReflectionProvider PROVIDER = new RuntimeReflectionProvider();

    private IClass<?> entityClass;
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

    public AuthenticatorBuilder(IDomainSecurityBuilder<E> domainBuilder, IClass<?> entityClass) {
        super(domainBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IAuthenticatorBuilder login(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.login = FieldResolver
                .fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder login(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.login = FieldResolver
                .fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder login(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.login = FieldResolver
                .fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.authorities = FieldResolver
                .fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.authorities = FieldResolver
                .fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.authorities = FieldResolver
                .fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(List.class)).address();

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

        this.credentialsNonExpired = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.credentialsNonExpired = FieldResolver
                .fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Boolean.class))
                .address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.credentialsNonExpired = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.enabled = FieldResolver
                .fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.enabled = FieldResolver
                .fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Boolean.class))
                .address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.enabled = FieldResolver
                .fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.accountNonExpired = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.accountNonExpired = FieldResolver
                .fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Boolean.class))
                .address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.accountNonExpired = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.accountNonLocked = FieldResolver
                .fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Boolean.class))
                .address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.accountNonLocked = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.accountNonLocked = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress,
                IClass.getClass(Boolean.class)).address();

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
        Objects.requireNonNull(authorization, "Authorization domain cannot be null");

        if (this.authenticatorAuthorizationBuilder == null) {
            this.authenticatorAuthorizationBuilder = new AuthenticatorAuthorizationBuilder<>(this, authorization);
        }

        return this.authenticatorAuthorizationBuilder;
    }

    @Override
    protected synchronized IAuthenticatorContext doBuild() throws ApiException {
        List<IAuthenticationDefinition> authenticationDefinitions = new ArrayList<>();
        for (IAuthenticationBuilder builder : this.selectedAuthentications) {
            authenticationDefinitions.add(builder.build().getAuthenticationDefinition());
        }

        // Build authorization definition if configured
        var authorizationDef = this.authenticatorAuthorizationBuilder != null
                ? this.authenticatorAuthorizationBuilder.build().getAuthenticatorAuthorizationDefinition()
                : null;

        IAuthenticatorDefinition authenticatorDefinition = new AuthenticatorDefintion(
                this.alwaysEnabled,
                this.login,
                this.authorities,
                this.credentialsNonExpired,
                this.enabled,
                this.accountNonLocked,
                this.accountNonExpired,
                this.scope,
                null,
                authenticationDefinitions,
                authorizationDef);

        return new AuthenticatorContext(authenticatorDefinition);
    }

    boolean hasAuthorizationConfig() {
        return this.authenticatorAuthorizationBuilder != null;
    }

    @Override
    protected void doAutoDetection() throws ApiException {

    }

}
