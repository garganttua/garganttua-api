package com.garganttua.api.core.builder;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.core.supply.IObjectSupplierBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;

public class DomainSecurityAuthorizationBuilder
        extends AbstractAutomaticLinkedBuilder<Object, IDomainSecurityAuthorizationBuilder, IDomainSecurityBuilder>
        implements IDomainSecurityAuthorizationBuilder {

    private @Nonnull IDomainBuilder authorizationDomain;
    private @Nonnull List<IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>>> interfaces;
    private IAuthorizationProtocolBuilder protocol;

    public DomainSecurityAuthorizationBuilder(DomainSecurityBuilder domainSecurityBuilder,
            IDomainBuilder authorizationDomain, List<IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>>> interfaces) {
        super(domainSecurityBuilder);
        this.authorizationDomain = Objects.requireNonNull(authorizationDomain, "AuthorizationDomain cannot be null");
        this.interfaces = Objects.requireNonNull(interfaces, "Interfaces cannot be null");
    }

    @Override
    public IDomainSecurityAuthorizationBuilder interfasse(Class<? extends IInterface> interfaceClass)
            throws DslException {
        Objects.requireNonNull(interfaceClass, "Interface class cannot be null");
        if (this.interfaces.stream().noneMatch(i -> i.getObjectClass().equals(interfaceClass))) {
            throw new DslException("Interface " + interfaceClass.getName() + " is not part of the domain");
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
