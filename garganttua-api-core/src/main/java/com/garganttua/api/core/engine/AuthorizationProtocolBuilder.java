package com.garganttua.api.core.engine;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.engine.IContextSecurityBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;

public class AuthorizationProtocolBuilder implements IAuthorizationProtocolBuilder {

    private Boolean autoDetect = false;
    private @Nonnull IObjectSupplier<?> supplier;
    private @Nonnull IContextSecurityBuilder contextSecurityBuilder;

    public AuthorizationProtocolBuilder(IContextSecurityBuilder contextSecurityBuilder, IObjectSupplier<?> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "Supplier class cannot be null");
        this.contextSecurityBuilder = Objects.requireNonNull(contextSecurityBuilder,
                "Context security builder cannot be null");
    }

    @Override
    public Object build() throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

    @Override
    public IContextSecurityBuilder up() {
        return this.contextSecurityBuilder;
    }

    @Override
    public IAuthorizationProtocolBuilder autoDetect(boolean b) throws CoreException {
        this.autoDetect = Objects.requireNonNull(b, "AutoDetect cannot be null");
        return this;
    }

}
