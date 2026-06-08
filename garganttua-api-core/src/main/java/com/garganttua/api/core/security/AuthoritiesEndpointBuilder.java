package com.garganttua.api.core.security;
import com.garganttua.api.core.api.ApiBuilder;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Objects;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IAuthoritiesEndpoint;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IAuthoritiesEndpointBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.core.security.AuthoritiesEndpoint;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;

/**
 * Records the user's choices for the "list authorities" endpoint and
 * produces an {@link IAuthoritiesEndpoint} descriptor consumed by
 * {@link ApiBuilder#doBuild()}.
 *
 * <p>Defaults — when the user calls {@code .exposeAuthorities().up()} with
 * no further setter — are {@link Access#authenticated} and no authority
 * gate. This matches the typical "let any authenticated user introspect"
 * stance while keeping anonymous lookups out.
 */
@Reflected
public class AuthoritiesEndpointBuilder
        extends AbstractAutomaticLinkedBuilder<IAuthoritiesEndpointBuilder, IApiBuilder, IAuthoritiesEndpoint>
        implements IAuthoritiesEndpointBuilder {

    private Access access = Access.authenticated;
    private String authority;

    public AuthoritiesEndpointBuilder(IApiBuilder apiBuilder) {
        super(apiBuilder);
    }

    @Override
    public IAuthoritiesEndpointBuilder access(Access access) throws ApiException {
        this.access = Objects.requireNonNull(access, "Access cannot be null");
        return this;
    }

    @Override
    public IAuthoritiesEndpointBuilder authority(String authority) throws ApiException {
        if (authority == null || authority.isBlank()) {
            throw new ApiException("Authority name cannot be null or blank");
        }
        this.authority = authority;
        return this;
    }

    @Override
    protected synchronized IAuthoritiesEndpoint doBuild() throws ApiException {
        return new AuthoritiesEndpoint(this.access, this.authority);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No annotation-driven auto-detection: this endpoint is purely
        // opt-in via the DSL.
    }
}
