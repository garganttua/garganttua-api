package com.garganttua.api.core.security;
import com.garganttua.api.core.security.authentication.AuthenticationBuilder;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import com.garganttua.api.core.security.ApiSecurityContext;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.commons.security.IApiSecurityContext;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class SecurityBuilder
        extends
        AbstractAutomaticLinkedBuilder<IApiSecurityBuilder, IApiBuilder, IApiSecurityContext>
        implements IApiSecurityBuilder {

    private Set<String> packages;
    private Map<IClass<?>, IAuthenticationBuilder<IApiSecurityBuilder>> authentications = new HashMap<>();
    private boolean disabled = false;

    public SecurityBuilder(Set<String> packages, IApiBuilder up) {
        super(up);
        this.packages = packages;
    }

    @Override
    public IAuthenticationBuilder<IApiSecurityBuilder> authentication(ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException {
        Objects.requireNonNull(supplier, "Authentication class cannot be null");
        Objects.requireNonNull(supplier.getSuppliedClass(), "Supplier should provide an object class");

        IAuthenticationBuilder<IApiSecurityBuilder> builder;
        if (!this.authentications.containsKey(supplier.getSuppliedClass())) {
            builder = new AuthenticationBuilder<IApiSecurityBuilder>(this, supplier);
            this.authentications.put(supplier.getSuppliedClass(), builder);
        } else {
            builder = this.authentications.get(supplier.getSuppliedClass());
        }
        return builder;
    }

    @Override
    public Optional<IAuthenticationBuilder<IApiSecurityBuilder>> isAuthenticationAvailable(IClass<?> authenticationClass) {
        IAuthenticationBuilder<IApiSecurityBuilder> builder = this.authentications.get(authenticationClass);
        return Optional.ofNullable(builder);
    }

    @Override
    public IAuthenticationBuilder<IApiSecurityBuilder> authentication(IClass<?> authenticationClass) throws ApiException {
        return isAuthenticationAvailable(authenticationClass)
                .orElseThrow(() -> new ApiException("No authentication found for class " + authenticationClass.getName()));
    }

    @Override
    protected synchronized IApiSecurityContext doBuild() throws ApiException {
        // Build all authentication contexts
        for (IAuthenticationBuilder<IApiSecurityBuilder> authBuilder : this.authentications.values()) {
            authBuilder.build();
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
    public IApiSecurityBuilder disable(boolean b) {
        this.disabled = b;
        return this;
    }

    @Override
    public IApiSecurityBuilder withPackage(String packageName) {
        this.packages.add(packageName);
        return this;
    }

    @Override
    public IApiSecurityBuilder withPackages(String[] packageNames) {
        for (String pkg : packageNames) {
            this.packages.add(pkg);
        }
        return this;
    }

    @Override
    public String[] getPackages() {
        return this.packages.toArray(new String[0]);
    }

    /**
     * Returns a map of authentication class → built IAuthenticationContext.
     * Must be called after build().
     */
    public Map<IClass<?>, IAuthenticationBuilder<IApiSecurityBuilder>> getAuthenticationBuilders() {
        return this.authentications;
    }

}
