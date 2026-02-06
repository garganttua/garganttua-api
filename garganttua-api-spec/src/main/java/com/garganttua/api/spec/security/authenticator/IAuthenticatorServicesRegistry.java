package com.garganttua.api.spec.security.authenticator;

import java.util.List;

import com.garganttua.api.spec.definition.IDomainDefinition;

public interface IAuthenticatorServicesRegistry {

	/* List<IService> getServices(); */

	List<IDomainDefinition<?>> getDomainDefinitions();

	/* Pair<AuthenticatorInfos, IService> getService(String domain); */

}
