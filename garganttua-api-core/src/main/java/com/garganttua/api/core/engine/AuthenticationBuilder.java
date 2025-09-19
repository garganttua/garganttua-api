package com.garganttua.api.core.engine;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthenticationBuilder;
import com.garganttua.api.spec.engine.IContextSecurityBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.reflection.GGObjectAddress;

public class AuthenticationBuilder implements IAuthenticationBuilder {

    private Boolean autoDetect = false;
    private @Nonnull ContextSecurityBuilder contextSecurityBuilder;
    private Boolean findPrincipal;
    private @Nonnull IObjectSupplier<?> supplier;

    public AuthenticationBuilder(ContextSecurityBuilder contextSecurityBuilder, IObjectSupplier<?> supplier) {
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
    public IAuthenticationBuilder autoDetect(boolean b) throws CoreException {
        this.autoDetect = Objects.requireNonNull(b, "AutoDetect cannot be null");
        return this;
    }

    @Override
    public IAuthenticationBuilder findPrincipal(boolean b) {
        this.findPrincipal = Objects.requireNonNull(b, "Find principal cannot be null");
        return this;
    }

    @Override
    public IAuthenticationBuilder authenticate(String methodName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
    }

    @Override
    public IAuthenticationBuilder authenticate(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
    }

    @Override
    public IAuthenticationBuilder authenticate(GGObjectAddress methodAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(String methodName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applySecurityOnEntity'");
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applySecurityOnEntity'");
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(GGObjectAddress methodAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applySecurityOnEntity'");
    }

    
}
