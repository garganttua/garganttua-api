package com.garganttua.api.core.builder;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityAuthorizationBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class DomainSecurityAuthorizationBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IDomainSecurityAuthorizationBuilder<E>, IDomainSecurityBuilder<E>, Object>
        implements IDomainSecurityAuthorizationBuilder<E> {

    private @Nonnull IDomainBuilder<E> authorizationDomain;
    private @Nonnull List<ISupplierBuilder<?, ? extends ISupplier<?>>> interfaces;
    private IAuthorizationProtocolBuilder protocol;

    public DomainSecurityAuthorizationBuilder(DomainSecurityBuilder<E> domainSecurityBuilder,
            IDomainBuilder<E> authorizationDomain, List<ISupplierBuilder<?, ? extends ISupplier<?>>> interfaces) {
        super(domainSecurityBuilder);
        this.authorizationDomain = Objects.requireNonNull(authorizationDomain, "AuthorizationDomain cannot be null");
        this.interfaces = Objects.requireNonNull(interfaces, "Interfaces cannot be null");
    }

    @Override
    public IDomainSecurityAuthorizationBuilder<E> interfasse(Class<? extends IInterface> interfaceClass)
            throws ApiException {
        Objects.requireNonNull(interfaceClass, "Interface class cannot be null");
        if (this.interfaces.stream().noneMatch(i -> i.getSuppliedClass().equals(interfaceClass))) {
            throw new ApiException("Interface " + interfaceClass.getName() + " is not part of the domain");
        }
        return this;
    }

    @Override
    public IDomainSecurityAuthorizationBuilder<E> protocol(IAuthorizationProtocolBuilder protocol) {
        this.protocol = Objects.requireNonNull(protocol, "Protocol cannot be null");
        return this;
    }

    @Override
    protected synchronized Object doBuild() {
        return null;
    }

    @Override
    protected void doAutoDetection() {

    }

}
