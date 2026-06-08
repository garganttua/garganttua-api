package com.garganttua.api.core.security;

import com.garganttua.api.commons.context.IAuthoritiesEndpoint;
import com.garganttua.api.commons.operation.Access;

/**
 * Pure descriptor for the framework-provided "list authorities" endpoint.
 * Built by {@link com.garganttua.api.core.security.AuthoritiesEndpointBuilder}
 * and surfaced via {@link com.garganttua.api.core.api.Api#getAuthoritiesEndpoint()}.
 */
public record AuthoritiesEndpoint(Access access, String authority) implements IAuthoritiesEndpoint {

}
