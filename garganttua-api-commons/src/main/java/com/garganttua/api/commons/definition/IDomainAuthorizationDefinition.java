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

}
