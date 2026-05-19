package com.garganttua.api.commons.context.dsl;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IAuthoritiesEndpoint;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

/**
 * DSL sub-builder reached via {@link IApiBuilder#exposeAuthorities()}.
 * Lets the user pick the access level (default {@link Access#authenticated})
 * and an optional authority name required to call the endpoint.
 *
 * <p>Both setters are optional. Calling {@code .exposeAuthorities().up()}
 * with no setters in between yields an endpoint that requires
 * {@code authenticated} callers and no specific authority.
 */
public interface IAuthoritiesEndpointBuilder
        extends IAutomaticLinkedBuilder<IAuthoritiesEndpointBuilder, IApiBuilder, IAuthoritiesEndpoint> {

	/**
	 * Sets the access level enforced for the endpoint. {@code null} is
	 * rejected. Default when never called: {@link Access#authenticated}.
	 */
	IAuthoritiesEndpointBuilder access(Access access) throws ApiException;

	/**
	 * Sets the authority name required on the caller. Blank or {@code null}
	 * is rejected. When not configured, any caller that passes the
	 * {@link #access(Access)} gate may call the endpoint.
	 *
	 * <p>Super-tenant and super-owner callers bypass this check.
	 */
	IAuthoritiesEndpointBuilder authority(String authority) throws ApiException;

}
