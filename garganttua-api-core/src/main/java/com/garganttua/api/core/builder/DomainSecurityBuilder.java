package com.garganttua.api.core.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.garganttua.api.core.context.application.DomainSecurityContext;
import com.garganttua.api.core.definition.DomainSecurityDefinition;
import com.garganttua.api.spec.engine.IAuthenticatorBuilder;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IUseCaseBuilder;
import com.garganttua.api.spec.security.IDomainSecurityAuthorizationBuilder;
import com.garganttua.api.spec.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.security.IDomainSecurityContext;
import com.garganttua.api.spec.security.IKeyBuilder;
import com.garganttua.api.spec.service.Access;
import com.garganttua.reflection.query.IGGObjectQuery;

public class DomainSecurityBuilder
        extends AbstractAutomaticLinkedBuilder<IDomainSecurityContext, IDomainSecurityBuilder, IDomainBuilder>
        implements IDomainSecurityBuilder {

    private @Nonnull List<IObjectSupplierBuilder<?>> interfaces;
    private Access creationAccess;
    private Access readAllAccess;
    private Access readOneAccess;
    private Access updateAccess;
    private Access deleteAllAccess;
    private Access deleteOneAccess;
    private Boolean deleteOneAuthority;
    private Boolean creationAuthority;
    private Boolean readAllAuthority;
    private Boolean readOneAuthority;
    private Boolean updateAuthority;
    private Boolean deleteAllAuthority;
    private IDomainSecurityAuthorizationBuilder domainSecurityAuthorizationBuilder;
    private boolean disabled = false;
    private IAuthorizationBuilder authorization;
    private IGGObjectQuery objectQuery;
    private Class<?> entityClass;
    private Map<IObjectSupplierBuilder<?>, IAuthorizationProtocolBuilder> authorizationProtocols = new HashMap<>();
    private IAuthenticatorBuilder authenticator;
    private IKeyBuilder key;

    public DomainSecurityBuilder(IDomainBuilder domainBuilder, List<IObjectSupplierBuilder<?>> interfaces,
            IGGObjectQuery objectQuery, Class<?> entityClass) {
        super(domainBuilder);
        this.objectQuery = Objects.requireNonNull(objectQuery, "Object query cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        this.interfaces = Objects.requireNonNull(interfaces, "Interfaces cannot be null");
    }

    @Override
    public IDomainSecurityBuilder creationAccess(Access access) {
        this.creationAccess = Objects.requireNonNull(access, "Access cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder readAllAccess(Access access) {
        this.readAllAccess = Objects.requireNonNull(access, "Access cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder readOneAccess(Access access) {
        this.readOneAccess = Objects.requireNonNull(access, "Access cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder updateAccess(Access access) {
        this.updateAccess = Objects.requireNonNull(access, "Access cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder deleteAllAccess(Access access) {
        this.deleteAllAccess = Objects.requireNonNull(access, "Access cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder deleteOneAccess(Access access) {
        this.deleteOneAccess = Objects.requireNonNull(access, "Access cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder creationAuthority(boolean authority) {
        this.creationAuthority = Objects.requireNonNull(authority, "Authority cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder readAllAuthority(boolean authority) {
        this.readAllAuthority = Objects.requireNonNull(authority, "Authority cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder readOneAuthority(boolean authority) {
        this.readOneAuthority = Objects.requireNonNull(authority, "Authority cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder updateAuthority(boolean authority) {
        this.updateAuthority = Objects.requireNonNull(authority, "Authority cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder deleteAllAuthority(boolean authority) {
        this.deleteAllAuthority = Objects.requireNonNull(authority, "Authority cannot be null");
        return this;
    }

    @Override
    public IDomainSecurityBuilder deleteOneAuthority(boolean authority) {
        this.deleteOneAuthority = Objects.requireNonNull(authority, "Authority cannot be null");
        return this;
    }

    @Override
    protected IDomainSecurityContext doBuild() {
        return new DomainSecurityContext(new DomainSecurityDefinition(this.creationAccess,
                this.readAllAccess,
                this.readOneAccess,
                this.updateAccess,
                this.deleteAllAccess,
                this.deleteOneAccess,
                this.deleteOneAuthority,
                this.creationAuthority,
                this.readAllAuthority,
                this.readOneAuthority,
                this.updateAuthority,
                this.deleteAllAuthority,
                this.disabled));
    }

    @Override
    protected void doAutoDetection() {

    }

    @Override
    public IDomainSecurityBuilder disable(boolean b) {
        this.disabled = b;
        return this;
    }

    @Override
    public IAuthorizationBuilder authorization() {
        if (this.authorization == null)
            this.authorization = new AuthorizationBuilder(this, this.objectQuery, this.entityClass);
        return this.authorization;
    }

    @Override
    public IDomainSecurityBuilder authorizationProtocol(Class<?> interfaceClass,
            IAuthorizationProtocolBuilder protocole) throws BuilderException {

        Optional<IObjectSupplierBuilder<?>> interfaceObjectSupplierBuilder = this.interfaces.stream()
                .filter(inter -> interfaceClass.isAssignableFrom(inter.getObjectClass())).findFirst();
        interfaceObjectSupplierBuilder.orElseThrow(() -> new BuilderException(
                "Interface object supplier builder not found for class " + interfaceClass.getSimpleName()));
        this.authorizationProtocols.put(interfaceObjectSupplierBuilder.get(), protocole);

        return this;
    }

    @Override
    public IKeyBuilder key() {
        if (this.key == null)
            this.key = new KeyBuilder(this, this.objectQuery, this.entityClass);
        return this.key;
    }

    @Override
    public IAuthenticatorBuilder authenticator() {
        if (this.authenticator == null)
            this.authenticator = new AuthenticatorBuilder(this, this.objectQuery, this.entityClass);
        return this.authenticator;
    }

    @Override
    public IDomainSecurityBuilder useCase(IUseCaseBuilder<?> useCaseBuilder, boolean authority, Access acceess) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'useCase'");
    }

}
