package com.garganttua.api.commons.definition;

import com.garganttua.core.reflection.ObjectAddress;

public interface IDomainAuthorizationDefinition {

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

	/**
	 * Method binder that produces the token (the mint-side dual of the
	 * verify-side authenticate method binder). {@code null} when none is declared
	 * — the framework then runs its standard minting (build entity + sign).
	 * Declared via {@code .authorization().issuer(supplier, "method").withParam(...)};
	 * the bound user method returns the produced authorization entity.
	 */
	default com.garganttua.core.reflection.binders.IMethodBinder<Object> issuerMethodBinder() {
		return null;
	}

}
