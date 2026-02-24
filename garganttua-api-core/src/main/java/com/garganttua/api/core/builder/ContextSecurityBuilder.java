package com.garganttua.api.core.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.core.context.application.ApiSecurityContext;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.context.dsl.security.IApiContextSecurityBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.security.IApiSecurityContext;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class ContextSecurityBuilder
        extends
        AbstractAutomaticLinkedBuilder<IApiContextSecurityBuilder, IApiContextBuilder, IApiSecurityContext>
        implements IApiContextSecurityBuilder {

    private Set<String> packages;
    private Map<Class<?>, IAuthorizationProtocolBuilder> protocols = new HashMap<Class<?>, IAuthorizationProtocolBuilder>();
    private Map<Class<?>, IAuthenticationBuilder> authentications = new HashMap<Class<?>, IAuthenticationBuilder>();
    private boolean disabled = false;

    public ContextSecurityBuilder(Set<String> packages, IApiContextBuilder up) {
        super(up);
        this.packages = packages;
    }

    @Override
    public IAuthenticationBuilder authentication(ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException {
        Objects.requireNonNull(supplier, "Authentication class cannot be null");
        Objects.requireNonNull(supplier.getSuppliedClass(), "Supplier should provide an object class");

        IAuthenticationBuilder builder;
        if (!this.authentications.containsKey(supplier.getSuppliedClass())) {
            builder = new AuthenticationBuilder(this);
            this.authentications.put(supplier.getSuppliedClass(), builder);
        } else {
            builder = this.authentications.get(supplier.getSuppliedClass());
        }
        return builder;
    }

    @Override
    public IAuthorizationProtocolBuilder authorizationProtocol(ISupplierBuilder<?, ? extends ISupplier<?>> supplier)
            throws ApiException {
        Objects.requireNonNull(supplier, "Supplier class cannot be null");
        Objects.requireNonNull(supplier.getSuppliedClass(), "Supplier should provide an object class");

        IAuthorizationProtocolBuilder builder;
        if (!this.protocols.containsKey(supplier.getSuppliedClass())) {
            builder = new AuthorizationProtocolBuilder(this, supplier);
            this.protocols.put(supplier.getSuppliedClass(), builder);
        } else {
            builder = this.protocols.get(supplier.getSuppliedClass());
        }
        return builder;
    }

    @Override
    public Optional<IAuthenticationBuilder> isAuthenticationAvailable(Class<?> authenticationClass) {
        return Optional.ofNullable(this.authentications.get(authenticationClass));
    }

    @Override
    public IAuthenticationBuilder authentication(Class<?> authenticationClass) throws ApiException {
        IAuthenticationBuilder authenticationBuilder = this.authentications.get(authenticationClass);
        if (authenticationBuilder != null) {
            return authenticationBuilder;
        }
        throw new ApiException("No authentication found for class " + authenticationClass.getName());
    }

    @Override
    public IAuthorizationProtocolBuilder authorizationProtocol(Class<?> authorizationProtocolClass)
            throws ApiException {
        IAuthorizationProtocolBuilder protocol = this.protocols.get(authorizationProtocolClass);
        if (protocol != null) {
            return protocol;
        }
        throw new ApiException("No protocol found for class " + authorizationProtocolClass.getName());
    }

    @Override
    protected synchronized IApiSecurityContext doBuild() throws ApiException {
        // Build all authentication and protocol contexts
        for (IAuthenticationBuilder authBuilder : this.authentications.values()) {
            authBuilder.build();
        }
        for (IAuthorizationProtocolBuilder protocolBuilder : this.protocols.values()) {
            protocolBuilder.build();
        }

        return new ApiSecurityContext(this.disabled);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        if (this.packages == null) {
            throw new ApiException(
                    "Packages must be set before setting autoDetect");
        }
    }

    @Override
    public IApiContextSecurityBuilder disable(boolean b) {
        this.disabled = b;
        return this;
    }

    @Override
    public IApiContextSecurityBuilder withPackage(String packageName) {
        this.packages.add(packageName);
        return this;
    }

    @Override
    public IApiContextSecurityBuilder withPackages(String[] packageNames) {
        for (String pkg : packageNames) {
            this.packages.add(pkg);
        }
        return this;
    }

    @Override
    public String[] getPackages() {
        return this.packages.toArray(new String[0]);
    }

}
