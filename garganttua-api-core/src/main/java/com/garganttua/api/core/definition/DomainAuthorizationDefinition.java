package com.garganttua.api.core.definition;

import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

public record DomainAuthorizationDefinition(
		ObjectAddress type,
		ObjectAddress authorities,
		ObjectAddress expiration,
		ObjectAddress creation,
		ObjectAddress revoked,
		boolean storable,
		boolean signable,
		boolean refreshable,
		ObjectAddress signatureField,
		ObjectAddress getDataToSignMethod,
		ObjectAddress refreshExpiration,
		ObjectAddress refreshRevoked,
		ObjectAddress encodeMethod,
		ObjectAddress decodeMethod,
		ObjectAddress signedBy,
		IMethodBinder<Object> issuerMethodBinder) implements IDomainAuthorizationDefinition {

}
