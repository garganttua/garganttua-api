package com.garganttua.api.core.security;
import com.garganttua.api.core.domain.DomainBuilder;
import com.garganttua.api.core.security.authenticator.AuthenticatorBuilder;
import com.garganttua.api.core.security.authorization.AuthorizationBuilder;
import com.garganttua.api.core.security.key.DomainKeyBuilder;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.core.security.DomainSecurityContext;
import com.garganttua.api.core.security.DomainSecurityDefinition;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.security.context.IAuthenticatorContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.commons.endpoint.IInterface;
import com.garganttua.api.commons.security.IDomainSecurityContext;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class DomainSecurityBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IDomainSecurityBuilder<E>, IDomainBuilder<E>, IDomainSecurityContext>
        implements IDomainSecurityBuilder<E> {

    private List<ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>>> interfaces;
    private boolean disabled = false;
    private IAuthorizationBuilder authorization;
    private IClass<?> entityClass;
    private IAuthenticatorBuilder authenticator;
    private com.garganttua.api.commons.context.dsl.IDomainKeyBuilder<E> keyBuilder;

    public DomainSecurityBuilder(IDomainBuilder<E> domainBuilder,
            List<ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>>> interfaces,
            IClass<?> entityClass) {
        super(domainBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        this.interfaces = Objects.requireNonNull(interfaces, "Interfaces cannot be null");
    }

    @Override
    protected synchronized IDomainSecurityContext doBuild() {
        return new DomainSecurityContext(buildSecurityDefinition());
    }

    public DomainSecurityDefinition buildSecurityDefinition() {
        IAuthenticatorDefinition authenticatorDefinition = null;
        if (this.authenticator != null) {
            IAuthenticatorContext authenticatorContext = (IAuthenticatorContext) this.authenticator.build();
            authenticatorDefinition = authenticatorContext.getAuthenticatorDefinition();
        }
        com.garganttua.api.commons.definition.IDomainAuthorizationDefinition authorizationDefinition = null;
        if (this.authorization != null) {
            var authorizationContext = (com.garganttua.api.commons.security.context.IAuthorizationContext) this.authorization.build();
            if (authorizationContext != null) {
                authorizationDefinition = authorizationContext.getAuthorizationDefinition();
            }
        }
        return new DomainSecurityDefinition(this.disabled, authenticatorDefinition, authorizationDefinition);
    }

    public boolean hasAuthenticator() {
        return this.authenticator != null;
    }

    public boolean hasAuthorization() {
        return this.authorization != null;
    }

    public IAuthenticatorBuilder<E> getAuthenticator() {
        return this.authenticator;
    }

    @Override
    protected void doAutoDetection() {

    }

    @Override
    public IDomainSecurityBuilder<E> disable(boolean b) {
        this.disabled = b;
        return this;
    }

    @Override
    public IAuthorizationBuilder authorization() {
        if (this.authorization == null)
            this.authorization = new AuthorizationBuilder(this, this.entityClass);
        return this.authorization;
    }

    @Override
    public IAuthenticatorBuilder authenticator() {
        if (this.authenticator == null)
            this.authenticator = new AuthenticatorBuilder(this, this.entityClass);
        return this.authenticator;
    }

    @Override
    public com.garganttua.api.commons.context.dsl.IDomainKeyBuilder<E> key() throws ApiException {
        if (this.keyBuilder == null)
            this.keyBuilder = new DomainKeyBuilder<>(this, this.entityClass);
        return this.keyBuilder;
    }

    /**
     * Builds the @Key domain definition declared via {@code .key()}, or
     * {@code null} when this domain is not a key domain. Read by the parent
     * {@link DomainBuilder} to populate {@code DomainDefinition.keyDefinition()}.
     */
    public com.garganttua.api.commons.definition.IDomainKeyDefinition buildKeyDefinition() {
        return this.keyBuilder == null ? null : this.keyBuilder.build().getKeyDefinition();
    }

    boolean isDisabled() {
        return this.disabled;
    }

    /**
     * Whether the domain's security pipeline (VERIFY_AUTHORIZATION / VERIFY_TENANT /
     * VERIFY_OWNER / VERIFY_AUTHORITY) is installed. Security is ON by default and the
     * per-operation access level governs (default {@code authenticated}); a domain
     * opts OUT of the whole gate explicitly via {@code .security().disable(true)}, and
     * opens individual operations via {@code Access.anonymous}. This is a fail-safe
     * default: a domain with no security mention still requires a valid token.
     */
    public boolean isSecurityEnabled() {
        return !this.disabled;
    }

    // --- CRUD access ---

    private IDomainSecurityBuilder<E> setCrudAccess(String label, Access access) {
        up().workflow(label).security().access(access);
        return this;
    }

    @Override
    public IDomainSecurityBuilder<E> creationAccess(Access access) {
        return setCrudAccess(BusinessOperation.create.getLabel(), access);
    }

    @Override
    public IDomainSecurityBuilder<E> readAllAccess(Access access) {
        return setCrudAccess(BusinessOperation.readAll.getLabel(), access);
    }

    @Override
    public IDomainSecurityBuilder<E> readOneAccess(Access access) {
        return setCrudAccess(BusinessOperation.readOne.getLabel(), access);
    }

    @Override
    public IDomainSecurityBuilder<E> updateAccess(Access access) {
        return setCrudAccess(BusinessOperation.update.getLabel(), access);
    }

    @Override
    public IDomainSecurityBuilder<E> deleteOneAccess(Access access) {
        return setCrudAccess(BusinessOperation.deleteOne.getLabel(), access);
    }

    @Override
    public IDomainSecurityBuilder<E> deleteAllAccess(Access access) {
        return setCrudAccess(BusinessOperation.deleteAll.getLabel(), access);
    }

    // --- CRUD authority (boolean) ---

    private IDomainSecurityBuilder<E> setCrudAuthority(String label, boolean authority) {
        up().workflow(label).security().authority(authority);
        return this;
    }

    @Override
    public IDomainSecurityBuilder<E> creationAuthority(boolean authority) {
        return setCrudAuthority(BusinessOperation.create.getLabel(), authority);
    }

    @Override
    public IDomainSecurityBuilder<E> readAllAuthority(boolean authority) {
        return setCrudAuthority(BusinessOperation.readAll.getLabel(), authority);
    }

    @Override
    public IDomainSecurityBuilder<E> readOneAuthority(boolean authority) {
        return setCrudAuthority(BusinessOperation.readOne.getLabel(), authority);
    }

    @Override
    public IDomainSecurityBuilder<E> updateAuthority(boolean authority) {
        return setCrudAuthority(BusinessOperation.update.getLabel(), authority);
    }

    @Override
    public IDomainSecurityBuilder<E> deleteOneAuthority(boolean authority) {
        return setCrudAuthority(BusinessOperation.deleteOne.getLabel(), authority);
    }

    @Override
    public IDomainSecurityBuilder<E> deleteAllAuthority(boolean authority) {
        return setCrudAuthority(BusinessOperation.deleteAll.getLabel(), authority);
    }

    // --- CRUD authority (custom String) ---

    private IDomainSecurityBuilder<E> setCrudAuthority(String label, String customAuthority) {
        up().workflow(label).security().authority(customAuthority);
        return this;
    }

    @Override
    public IDomainSecurityBuilder<E> creationAuthority(String authority) {
        return setCrudAuthority(BusinessOperation.create.getLabel(), authority);
    }

    @Override
    public IDomainSecurityBuilder<E> readAllAuthority(String authority) {
        return setCrudAuthority(BusinessOperation.readAll.getLabel(), authority);
    }

    @Override
    public IDomainSecurityBuilder<E> readOneAuthority(String authority) {
        return setCrudAuthority(BusinessOperation.readOne.getLabel(), authority);
    }

    @Override
    public IDomainSecurityBuilder<E> updateAuthority(String authority) {
        return setCrudAuthority(BusinessOperation.update.getLabel(), authority);
    }

    @Override
    public IDomainSecurityBuilder<E> deleteOneAuthority(String authority) {
        return setCrudAuthority(BusinessOperation.deleteOne.getLabel(), authority);
    }

    @Override
    public IDomainSecurityBuilder<E> deleteAllAuthority(String authority) {
        return setCrudAuthority(BusinessOperation.deleteAll.getLabel(), authority);
    }

}
