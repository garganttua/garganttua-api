package com.garganttua.api.core.builder;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.engine.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.security.IDomainSecurityAuthorizationBuilder;
import com.garganttua.api.spec.security.IDomainSecurityBuilder;

public class DomainSecurityAuthorizationBuilder
        extends AbstractAutomaticLinkedBuilder<Object, IDomainSecurityAuthorizationBuilder, IDomainSecurityBuilder>
        implements IDomainSecurityAuthorizationBuilder {

    private @Nonnull IDomainBuilder authorizationDomain;
    private @Nonnull List<IObjectSupplierBuilder<?>> interfaces;
    private IAuthorizationProtocolBuilder protocol;

    public DomainSecurityAuthorizationBuilder(DomainSecurityBuilder domainSecurityBuilder,
            IDomainBuilder authorizationDomain, List<IObjectSupplierBuilder<?>> interfaces) {
        super(domainSecurityBuilder);
        this.authorizationDomain = Objects.requireNonNull(authorizationDomain, "AuthorizationDomain cannot be null");
        this.interfaces = Objects.requireNonNull(interfaces, "Interfaces cannot be null");
    }

    @Override
    public IDomainSecurityAuthorizationBuilder interfasse(Class<? extends IInterface> interfaceClass)
            throws BuilderException {
        Objects.requireNonNull(interfaceClass, "Interface class cannot be null");
        if (this.interfaces.stream().noneMatch(i -> i.getObjectClass().equals(interfaceClass))) {
            throw new BuilderException("Interface " + interfaceClass.getName() + " is not part of the domain");
        }
        return this;
    }

    @Override
    public IDomainSecurityAuthorizationBuilder protocol(IAuthorizationProtocolBuilder protocol) {
        this.protocol = Objects.requireNonNull(protocol, "Protocol cannot be null");
        return this;
    }

    @Override
    protected Object doBuild() {
        return null;
    }

    @Override
    protected void doAutoDetection() {

    }

}
