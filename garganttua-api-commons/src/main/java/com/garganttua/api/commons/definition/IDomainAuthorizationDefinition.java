package com.garganttua.api.commons.definition;

import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

public interface IDomainAuthorizationDefinition {

	/**
	 * Custom caller-reconciliation binder, declared via
	 * {@code .security().authorization().reconcile(supplier, "method")}. When present,
	 * the verify pipeline calls this user method ({@code ICaller method(IAuthentication, ICaller)})
	 * instead of the default {@link com.garganttua.api.commons.security.authentication.IAuthentication#reconcile}
	 * — enabling fully custom, self-contained caller resolution. {@code null} when not declared.
	 */
	default IMethodBinder<?> reconcileBinder() {
		return null;
	}

	ObjectAddress type();

	ObjectAddress authorities();

	ObjectAddress expiration();

	ObjectAddress creation();

	ObjectAddress revoked();

	boolean storable();

	boolean signable();

	boolean refreshable();

	ObjectAddress signatureField();

	ObjectAddress getDataToSignMethod();

	ObjectAddress refreshExpiration();

	ObjectAddress refreshRevoked();

	/**
	 * Method on the authorization entity that produces a transport-friendly
	 * encoded form (e.g. JWT compact serialization). Declared via
	 * {@code .refreshable().encode(method)}. {@code null} when not configured.
	 */
	ObjectAddress encodeMethod();

	/**
	 * Method that decodes a transport-friendly encoded authorization back into
	 * a typed entity. Declared via {@code .refreshable().decode(method)}.
	 * {@code null} when not configured.
	 */
	ObjectAddress decodeMethod();

	/**
	 * Field on the authorization entity recording who signed it, stamped at
	 * signing time with the qualified key-realm id ({@code ${domainName}:${id}}).
	 * {@code null} when not configured.
	 */
	ObjectAddress signedBy();

}
