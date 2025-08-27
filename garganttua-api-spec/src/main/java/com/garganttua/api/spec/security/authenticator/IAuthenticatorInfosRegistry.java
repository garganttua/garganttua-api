package com.garganttua.api.spec.security.authenticator;

import java.util.List;

import com.garganttua.api.spec.domain.IDomain;

public interface IAuthenticatorInfosRegistry {

	List<AuthenticatorInfos> getAuthenticatorInfos();

	List<IDomain> getDomains();

	AuthenticatorInfos getAuthenticatorInfos(String domain);

}
