package com.garganttua.api.core.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.garganttua.api.core.context.application.ApplicationSecurityContext;
import com.garganttua.core.dsl.DslException;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.security.IApiSecurityContext;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.context.dsl.security.IApiContextSecurityBuilder;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;

public class ContextSecurityBuilder
        extends
        AbstractAutomaticLinkedBuilder<IApiContextSecurityBuilder, IApiContextBuilder, IApiSecurityContext>
        implements IApiContextSecurityBuilder {

    private List<String> packages;
    private Map<Class<?>, IAuthorizationProtocolBuilder> protocols = new HashMap<Class<?>, IAuthorizationProtocolBuilder>();
    private Map<Class<?>, IAuthenticationBuilder> authentications = new HashMap<Class<?>, IAuthenticationBuilder>();
    private boolean disabled = false;

    public ContextSecurityBuilder(List<String> packages, IApiContextBuilder up) {
        super(up);
        this.packages = packages;
    }

    @Override
    public IAuthenticationBuilder authentication(IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) throws DslException {
        Objects.requireNonNull(supplier, "Authentication class cannot be null");
        Objects.requireNonNull(supplier.getSuppliedType(), "Supplier should provide an object class");

        IAuthenticationBuilder builder;
        if (!this.authentications.containsKey(supplier.getSuppliedType())) {
            builder = new AuthenticationBuilder(this);
            this.authentications.put(supplier.getSuppliedType(), builder);
        } else {
            builder = this.authentications.get(supplier.getSuppliedType());
        }
        return builder;
    }

    @Override
    public IAuthorizationProtocolBuilder authorizationProtocol(IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier)
            throws DslException {
        Objects.requireNonNull(supplier, "Supplier class cannot be null");
        Objects.requireNonNull(supplier.getSuppliedType(), "Supplier should provide an object class");

        IAuthorizationProtocolBuilder builder;
        if (!this.protocols.containsKey(supplier.getSuppliedType())) {
            builder = new AuthorizationProtocolBuilder(this, supplier);
            this.protocols.put(supplier.getSuppliedType(), builder);
        } else {
            builder = this.protocols.get(supplier.getSuppliedType());
        }
        return builder;
    }

    @Override
    public Optional<IAuthenticationBuilder> isAuthenticationAvailable(Class<?> authenticationClass) {
        return Optional.ofNullable(this.authentications.get(authenticationClass));
    }

    @Override
    public IAuthenticationBuilder authentication(Class<?> authenticationClass) throws DslException {
        IAuthenticationBuilder authenticationBuilder = this.authentications.get(authenticationClass);
        if (authenticationBuilder != null) {
            return authenticationBuilder;
        }
        throw new DslException("No authentication found for class " + authenticationClass.getName());
    }

    @Override
    public IAuthorizationProtocolBuilder authorizationProtocol(Class<?> authorizationProtocolClass)
            throws DslException {
        IAuthorizationProtocolBuilder protocol = this.protocols.get(authorizationProtocolClass);
        if (protocol != null) {
            return protocol;
        }
        throw new DslException("No protocol found for class " + authorizationProtocolClass.getName());
    }

    @Override
    protected IApiSecurityContext doBuild() throws DslException {
        return new ApplicationSecurityContext(
                this.packages,
                this.authentications.values().stream().map(builder -> {
                    try {
                        return builder.build();
                    } catch (DslException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList()),
                this.protocols.values().stream().map(builder -> {
                    try {
                        return builder.build();
                    } catch (DslException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList()));
    }

    @Override
    protected void doAutoDetection() throws DslException {
        if (this.packages == null) {
            throw new DslException(
                    "Packages must be set before setting autoDetect");
        }
    }

    @Override
    public IApiContextSecurityBuilder disable(boolean b) {
        this.disabled = b;
        return this;
    }

}
