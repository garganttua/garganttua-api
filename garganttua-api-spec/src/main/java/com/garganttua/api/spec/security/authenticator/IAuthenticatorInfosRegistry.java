package com.garganttua.api.spec.security.authenticator;

import java.util.List;

import com.garganttua.api.spec.definition.IDomainDefinition;

public interface IAuthenticatorInfosRegistry {

	List<AuthenticatorInfos> getAuthenticatorInfos();

	List<IDomainDefinition<?>> getDomainDefinitions();

	AuthenticatorInfos getAuthenticatorInfos(String domain);

}
