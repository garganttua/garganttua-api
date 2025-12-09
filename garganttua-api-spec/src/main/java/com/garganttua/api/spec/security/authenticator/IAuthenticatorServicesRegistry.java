package com.garganttua.api.spec.security.authenticator;

import java.util.List;

import com.garganttua.api.spec.domain.IDomain;

public interface IAuthenticatorServicesRegistry {

	/* List<IService> getServices(); */

	List<IDomain> getDomains();

	/* Pair<AuthenticatorInfos, IService> getService(String domain); */

}
