package com.garganttua.api.core.definition;

import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.core.reflection.ObjectAddress;

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
		ObjectAddress signedBy) implements IDomainAuthorizationDefinition {

}
