package com.garganttua.api.core.security.authenticator;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.core.reflection.IField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.security.authenticator.AuthenticatorContext;
import com.garganttua.api.core.security.authenticator.AuthenticatorDefintion;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorMethodBinderBuilder;
import com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.commons.definition.IAuthenticationDefinition;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.api.commons.security.context.IAuthenticatorContext;
import com.garganttua.api.core.security.authentication.AuthenticationBuilder;
import com.garganttua.api.core.security.authenticator.AuthenticatorMethodBinderBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class AuthenticatorBuilder<E> extends
        AbstractAutomaticLinkedBuilder<IAuthenticatorBuilder<E>, IDomainSecurityBuilder<E>, IAuthenticatorContext>
        implements IAuthenticatorBuilder<E> {

    // Reflection provider is whatever the user installed via IClass.setReflection().
    // Resolved lazily per call so the framework never picks an implementation.
    private static IReflectionProvider provider() {
        return IClass.getReflection();
    }

    private IClass<?> entityClass;
    private boolean alwaysEnabled = false;
    private ObjectAddress login;
    private ObjectAddress authorities;
    private ObjectAddress credentialsNonExpired;
    private ObjectAddress enabled;
    private ObjectAddress accountNonLocked;
    private ObjectAddress accountNonExpired;
    private AuthenticatorScope scope;
    private List<IAuthenticationBuilder<?>> selectedAuthentications = new ArrayList<>();
    private IAuthenticatorAuthorizationBuilder<E> authenticatorAuthorizationBuilder;
    private IAuthenticatorMethodBinderBuilder<E> authorizationMethodBinderBuilder;

    public AuthenticatorBuilder(IDomainSecurityBuilder<E> domainBuilder, IClass<?> entityClass) {
        super(domainBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IAuthenticatorBuilder login(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.login = FieldResolver
                .fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder login(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.login = FieldResolver
                .fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder login(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.login = FieldResolver
                .fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.authorities = FieldResolver
                .fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.authorities = FieldResolver
                .fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder authorities(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.authorities = FieldResolver
                .fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder alwaysEnabled(boolean b) {
        this.alwaysEnabled = b;
        return this;
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.credentialsNonExpired = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.credentialsNonExpired = FieldResolver
                .fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(Boolean.class))
                .address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder credentialsNonExpired(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.credentialsNonExpired = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.enabled = FieldResolver
                .fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.enabled = FieldResolver
                .fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(Boolean.class))
                .address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder enabled(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.enabled = FieldResolver
                .fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.accountNonExpired = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.accountNonExpired = FieldResolver
                .fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(Boolean.class))
                .address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonExpired(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.accountNonExpired = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.accountNonLocked = FieldResolver
                .fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(Boolean.class))
                .address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.accountNonLocked = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder accountNonLocked(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.accountNonLocked = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress,
                IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthenticatorBuilder scope(AuthenticatorScope scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthenticationBuilder<E> authentication(
            IAuthenticationBuilder<?> authentication) throws ApiException {
        Objects.requireNonNull(authentication, "Authentication cannot be null");
        this.selectedAuthentications.add(authentication);
        return new AuthenticatorAuthentication<>(this);
    }

    @Override
    public IAuthenticationBuilder<IAuthenticatorBuilder<E>> authentication(
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException {
        Objects.requireNonNull(supplier, "Authentication supplier cannot be null");
        // A fresh authentication owned by — and linked back to — this authenticator
        // (up() returns the authenticator). Registered for the verify cascade.
        AuthenticationBuilder<IAuthenticatorBuilder<E>> authentication =
                new AuthenticationBuilder<>(this, supplier);
        this.selectedAuthentications.add(authentication);
        return authentication;
    }

    @Override
    public IAuthenticationBuilder<IAuthenticatorBuilder<E>> authentication(IClass<?> authenticationClass)
            throws ApiException {
        Objects.requireNonNull(authenticationClass, "Authentication class cannot be null");
        Object instance;
        try {
            instance = authenticationClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new ApiException("Failed to instantiate authentication class '"
                    + authenticationClass.getName() + "'. A public no-arg constructor is required.", e);
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ISupplierBuilder<?, ? extends ISupplier<?>> supplier = new FixedSupplierBuilder(instance, authenticationClass);
        return authentication(supplier);
    }

    /**
     * Creates and stores the token (authorization) domain sub-builder, parented to
     * this authenticator (so {@code .up()} returns here). Internal hook shared by
     * {@link AuthenticatorAuthentication#authorization(IDomainBuilder)} (the DSL
     * path) and the annotation scanner.
     */
    public IAuthenticatorAuthorizationBuilder tokenAuthorization(IDomainBuilder authorization) {
        Objects.requireNonNull(authorization, "Authorization domain cannot be null");

        if (this.authenticatorAuthorizationBuilder == null) {
            this.authenticatorAuthorizationBuilder = new AuthenticatorAuthorizationBuilder<>(this, authorization);
        }

        return this.authenticatorAuthorizationBuilder;
    }

    /**
     * Creates and stores the custom token-production (mint) binder, parented to
     * this authenticator (so {@code .up()} returns here). Called by
     * {@link AuthenticatorAuthentication#authorization} — the mint is declared
     * per-authentication in the DSL but stored once per authenticator.
     */
    IAuthenticatorMethodBinderBuilder<E> mintBinder(
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier, String methodName) throws ApiException {
        Objects.requireNonNull(supplier, "Issuer supplier cannot be null");
        Objects.requireNonNull(methodName, "Issuer method name cannot be null");
        this.authorizationMethodBinderBuilder = new AuthenticatorMethodBinderBuilder<>(this, supplier, methodName);
        return this.authorizationMethodBinderBuilder;
    }

    @Override
    protected synchronized IAuthenticatorContext doBuild() throws ApiException {
        List<IAuthenticationDefinition> authenticationDefinitions = new ArrayList<>();
        for (IAuthenticationBuilder<?> builder : this.selectedAuthentications) {
            authenticationDefinitions.add(builder.build().getAuthenticationDefinition());
        }

        // Build authorization definition if configured
        var authorizationDef = this.authenticatorAuthorizationBuilder != null
                ? this.authenticatorAuthorizationBuilder.build().getAuthenticatorAuthorizationDefinition()
                : null;

        // The custom token-production (mint) binder, declared via
        // .authorization(issuer, "method").withParam(...). Null → framework mints.
        IMethodBinder<?> authorizationMethodBinder = this.authorizationMethodBinderBuilder != null
                ? this.authorizationMethodBinderBuilder.build()
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
                authorizationDef,
                authorizationMethodBinder);

        return new AuthenticatorContext(authenticatorDefinition);
    }

    /**
     * True when this authenticator has ANY authorization declared — a token domain
     * ({@code .authorization(domain)}) or a custom mint issuer
     * ({@code .authorization(issuer, "method")}). Drives whether CREATE_AUTHORIZATION
     * runs after a successful authentication: with no authorization defined at all,
     * authentication succeeds but NO token is generated.
     */
    public boolean hasAuthorizationConfig() {
        return this.authenticatorAuthorizationBuilder != null
                || this.authorizationMethodBinderBuilder != null;
    }

    @Override
    protected void doAutoDetection() throws ApiException {

    }

}
