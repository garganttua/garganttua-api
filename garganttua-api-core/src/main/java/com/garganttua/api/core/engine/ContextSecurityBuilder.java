package com.garganttua.api.core.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IApplicationContextBuilder;
import com.garganttua.api.spec.engine.IAuthenticationBuilder;
import com.garganttua.api.spec.engine.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.engine.IContextSecurityBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;

public class ContextSecurityBuilder implements IContextSecurityBuilder {

    private @Nonnull IApplicationContextBuilder up;
    private List<String> packages;
    private boolean autoDetect;
    private Map<Class<?>, IAuthorizationProtocolBuilder> protocols = new HashMap<Class<?>, IAuthorizationProtocolBuilder>();
    private Map<Class<?>, IAuthenticationBuilder> authentications = new HashMap<Class<?>, IAuthenticationBuilder>();

    public ContextSecurityBuilder(List<String> packages, IApplicationContextBuilder up) {
        this.up = Objects.requireNonNull(up, "Up cannot be null");
        this.packages = packages;
    }

    @Override
    public Object build() throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

    @Override
    public IApplicationContextBuilder up() {
        return this.up;
    }

    @Override
    public IContextSecurityBuilder autoDetect(boolean b) throws CoreException {
        if (this.packages == null && b) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Packages must be set before setting autoDetect");
        }
        this.autoDetect = b;
        return this;
    }

    @Override
    public IAuthenticationBuilder authentication(Object authentication) throws CoreException {
        Objects.requireNonNull(authentication, "Authentication class cannot be null");

        IAuthenticationBuilder builder;
        if (!this.authentications.containsKey(authentication.getClass())) {
            builder = new AuthenticationBuilder(this, new ObjectSupplier<>(authentication));
            this.authentications.put(authentication.getClass(), builder);
        } else {
            builder = this.authentications.get(authentication.getClass());
        }
        return builder;
    }

    @Override
    public IAuthorizationProtocolBuilder authorizationProtocol(Object protocol) throws CoreException {
        Objects.requireNonNull(protocol, "Protocol class cannot be null");

        IAuthorizationProtocolBuilder builder;
        if (!this.protocols.containsKey(protocol.getClass())) {
            builder = new AuthorizationProtocolBuilder(this, new ObjectSupplier<>(protocol));
            this.protocols.put(protocol.getClass(), builder);
        } else {
            builder = this.protocols.get(protocol.getClass());
        }
        return builder;
    }

    @Override
    public IAuthenticationBuilder authentication(IObjectSupplier<?> supplier) throws CoreException {
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
    public IAuthorizationProtocolBuilder authorizationProtocol(IObjectSupplier<?> supplier) throws CoreException {
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

}
