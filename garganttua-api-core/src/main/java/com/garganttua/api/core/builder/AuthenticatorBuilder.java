package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.garganttua.api.core.builder.resolver.FieldResolver;
import com.garganttua.api.core.context.application.AuthenticatorContext;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthenticationBuilder;
import com.garganttua.api.spec.engine.IAuthenticatorAuthorizationBuilder;
import com.garganttua.api.spec.engine.IAuthenticatorBuilder;
import com.garganttua.api.spec.engine.IAuthenticatorContext;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.query.IGGObjectQuery;

public class AuthenticatorBuilder extends AbstractAutomaticLinkedBuilder<IAuthenticatorContext, IAuthenticatorBuilder, IDomainSecurityBuilder>
        implements IAuthenticatorBuilder {

    private @Nonnull IGGObjectQuery objectQuery;
    private @Nonnull Class<?> entityClass;
    private boolean alwaysEnabled = false;
    private GGObjectAddress login;
    private GGObjectAddress authorities;
    private GGObjectAddress credentialsNonExpired;
    private GGObjectAddress enabled;
    private GGObjectAddress accountNonLocked;
    private GGObjectAddress accountNonExpired;
    private AuthenticatorScope scope;
    private List<IAuthenticationBuilder> selectedAuthentications = new ArrayList<>();
    private IAuthenticatorAuthorizationBuilder authenticatorAuthorizationBuilder;

    public AuthenticatorBuilder(IDomainSecurityBuilder domainBuilder, IGGObjectQuery objectQuery, Class<?> entityClass) {
        super(domainBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
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
    public IAuthenticatorBuilder authentication(IAuthenticationBuilder authentication) throws BuilderException {
        Objects.requireNonNull(authentication, "Authentication cannot be null");
        this.selectedAuthentications.add(authentication);
        return this;
    }

    @Override
    public IAuthenticatorAuthorizationBuilder authorization(IDomainBuilder authorization) {
        Objects.requireNonNull(authorization, "Authentication cannot be null");

        if (this.authenticatorAuthorizationBuilder == null) {
            this.authenticatorAuthorizationBuilder = new AuthenticatorAuthorizationBuilder(this);
        }

        return this.authenticatorAuthorizationBuilder;
    }

    @Override
    protected IAuthenticatorContext doBuild() throws CoreException {
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
                    } catch (CoreException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList()),
                this.authenticatorAuthorizationBuilder.build());
    }

    @Override
    protected void doAutoDetection() throws CoreException {

    }

}
