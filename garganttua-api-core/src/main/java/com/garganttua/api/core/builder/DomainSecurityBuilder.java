package com.garganttua.api.core.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.core.context.security.DomainSecurityContext;
import com.garganttua.api.core.definition.DomainSecurityDefinition;
import com.garganttua.api.spec.definition.IAuthenticatorDefinition;
import com.garganttua.api.spec.operation.Access;
import com.garganttua.api.spec.operation.BusinessOperation;
import com.garganttua.api.spec.security.context.IAuthenticatorContext;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.context.dsl.security.IKeyBuilder;
import com.garganttua.api.spec.endpoint.IEndpoint;
import com.garganttua.api.spec.security.IDomainSecurityContext;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class DomainSecurityBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IDomainSecurityBuilder<E>, IDomainBuilder<E>, IDomainSecurityContext>
        implements IDomainSecurityBuilder<E> {

    private List<ISupplierBuilder<? extends IEndpoint, ? extends ISupplier<? extends IEndpoint>>> interfaces;
    private IDomainSecurityAuthorizationBuilder domainSecurityAuthorizationBuilder;
    private boolean disabled = false;
    private boolean hasCrudSecurityConfig = false;
    private IAuthorizationBuilder authorization;
    private IClass<?> entityClass;
    private Map<ISupplierBuilder<?, ? extends ISupplier<?>>, IAuthorizationProtocolBuilder> authorizationProtocols = new HashMap<>();
    private IAuthenticatorBuilder authenticator;
    private IKeyBuilder key;

    public DomainSecurityBuilder(IDomainBuilder<E> domainBuilder,
            List<ISupplierBuilder<? extends IEndpoint, ? extends ISupplier<? extends IEndpoint>>> interfaces,
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
        com.garganttua.api.spec.definition.IDomainAuthorizationDefinition authorizationDefinition = null;
        if (this.authorization != null) {
            var authorizationContext = (com.garganttua.api.spec.security.context.IAuthorizationContext) this.authorization.build();
            if (authorizationContext != null) {
                authorizationDefinition = authorizationContext.getAuthorizationDefinition();
            }
        }
        return new DomainSecurityDefinition(this.disabled, authenticatorDefinition, authorizationDefinition);
    }

    boolean hasAuthenticator() {
        return this.authenticator != null;
    }

    boolean hasAuthorization() {
        return this.authorization != null;
    }

    IAuthenticatorBuilder<E> getAuthenticator() {
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

    public IDomainSecurityBuilder<E> authorizationProtocol(IClass<?> interfaceClass,
            IAuthorizationProtocolBuilder protocole) throws ApiException {

        Optional<ISupplierBuilder<? extends IEndpoint, ? extends ISupplier<? extends IEndpoint>>> interfaceObjectSupplierBuilder = this.interfaces.stream()
                .filter(inter -> interfaceClass.isAssignableFrom(inter.getSuppliedClass())).findFirst();
        interfaceObjectSupplierBuilder.orElseThrow(() -> new ApiException(
                "Interface object supplier builder not found for class " + interfaceClass.getSimpleName()));
        this.authorizationProtocols.put(interfaceObjectSupplierBuilder.get(), protocole);

        return this;
    }

    @Override
    public IKeyBuilder key() {
        if (this.key == null)
            this.key = new KeyBuilder(this, this.entityClass);
        return this.key;
    }

    @Override
    public IAuthenticatorBuilder authenticator() {
        if (this.authenticator == null)
            this.authenticator = new AuthenticatorBuilder(this, this.entityClass);
        return this.authenticator;
    }

    @Override
    public IDomainSecurityBuilder<E> useCase(IUseCaseBuilder<?, ?, ?> useCaseBuilder, boolean authority, Access acceess) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'useCase'");
    }

    boolean isDisabled() {
        return this.disabled;
    }

    boolean hasSecurityConfiguration() {
        return !this.disabled && (this.authenticator != null || this.authorization != null || this.hasCrudSecurityConfig);
    }

    // --- CRUD access ---

    private IDomainSecurityBuilder<E> setCrudAccess(String label, Access access) {
        up().workflow(label).security().access(access);
        this.hasCrudSecurityConfig = true;
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
        this.hasCrudSecurityConfig = true;
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
        this.hasCrudSecurityConfig = true;
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
