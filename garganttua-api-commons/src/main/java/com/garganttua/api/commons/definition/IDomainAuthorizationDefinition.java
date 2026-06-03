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

}
