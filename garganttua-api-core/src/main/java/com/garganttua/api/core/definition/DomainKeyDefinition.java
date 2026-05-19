package com.garganttua.api.core.definition;

import com.garganttua.api.commons.definition.IDomainKeyDefinition;
import com.garganttua.core.reflection.ObjectAddress;

public record DomainKeyDefinition(
		ObjectAddress realmName,
		ObjectAddress algorithm,
		ObjectAddress signatureAlgorithm,
		ObjectAddress publicMaterial,
		ObjectAddress privateMaterial,
		ObjectAddress expiration,
		ObjectAddress revoked) implements IDomainKeyDefinition {

}
