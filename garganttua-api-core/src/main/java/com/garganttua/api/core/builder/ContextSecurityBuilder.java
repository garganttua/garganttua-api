package com.garganttua.api.core.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.garganttua.api.core.context.application.ApplicationSecurityContext;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContextBuilder;
import com.garganttua.api.spec.engine.IApplicationSecurityContext;
import com.garganttua.api.spec.engine.IAuthenticationBuilder;
import com.garganttua.api.spec.engine.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.engine.IContextSecurityBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;

public class ContextSecurityBuilder
        extends
        AbstractAutomaticLinkedBuilder<IApplicationSecurityContext, IContextSecurityBuilder, IApplicationContextBuilder>
        implements IContextSecurityBuilder {

    private List<String> packages;
    private Map<Class<?>, IAuthorizationProtocolBuilder> protocols = new HashMap<Class<?>, IAuthorizationProtocolBuilder>();
    private Map<Class<?>, IAuthenticationBuilder> authentications = new HashMap<Class<?>, IAuthenticationBuilder>();
    private boolean disabled = false;

    public ContextSecurityBuilder(List<String> packages, IApplicationContextBuilder up) {
        super(up);
        this.packages = packages;
    }

    @Override
    public IAuthenticationBuilder authentication(IObjectSupplierBuilder<?> supplier) throws CoreException {
        Objects.requireNonNull(supplier, "Authentication class cannot be null");
        Objects.requireNonNull(supplier.getObjectClass(), "Supplier should provide an object class");

        IAuthenticationBuilder builder;
        if (!this.authentications.containsKey(supplier.getObjectClass())) {
            builder = new AuthenticationBuilder(this, supplier);
            this.authentications.put(supplier.getObjectClass(), builder);
        } else {
            builder = this.authentications.get(supplier.getObjectClass());
        }
        return builder;
    }

    @Override
    public IAuthorizationProtocolBuilder authorizationProtocol(IObjectSupplierBuilder<?> supplier)
            throws CoreException {
        Objects.requireNonNull(supplier, "Supplier class cannot be null");
        Objects.requireNonNull(supplier.getObjectClass(), "Supplier should provide an object class");

        IAuthorizationProtocolBuilder builder;
        if (!this.protocols.containsKey(supplier.getObjectClass())) {
            builder = new AuthorizationProtocolBuilder(this, supplier);
            this.protocols.put(supplier.getObjectClass(), builder);
        } else {
            builder = this.protocols.get(supplier.getObjectClass());
        }
        return builder;
    }

    @Override
    public Optional<IAuthenticationBuilder> isAuthenticationAvailable(Class<?> authenticationClass) {
        return Optional.ofNullable(this.authentications.get(authenticationClass));
    }

    @Override
    public IAuthenticationBuilder authentication(Class<?> authenticationClass) throws CoreException {
        IAuthenticationBuilder authenticationBuilder = this.authentications.get(authenticationClass);
        if (authenticationBuilder != null) {
            return authenticationBuilder;
        }
        throw new BuilderException("No authentication found for class " + authenticationClass.getName());
    }

    @Override
    public IAuthorizationProtocolBuilder authorizationProtocol(Class<?> authorizationProtocolClass)
            throws CoreException {
        IAuthorizationProtocolBuilder protocol = this.protocols.get(authorizationProtocolClass);
        if (protocol != null) {
            return protocol;
        }
        throw new BuilderException("No protocol found for class " + authorizationProtocolClass.getName());
    }

    @Override
    protected IApplicationSecurityContext doBuild() throws BuilderException {
        return new ApplicationSecurityContext(
                this.packages,
                this.authentications.values().stream().map(builder -> {
                    try {
                        return builder.build();
                    } catch (CoreException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList()),
                this.protocols.values().stream().map(builder -> {
                    try {
                        return builder.build();
                    } catch (CoreException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList()));
    }

    @Override
    protected void doAutoDetection() throws BuilderException {
        if (this.packages == null) {
            throw new BuilderException(
                    "Packages must be set before setting autoDetect");
        }
    }

    @Override
    public IContextSecurityBuilder disable(boolean b) {
        this.disabled = b;
        return this;
    }

}
