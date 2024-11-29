package com.garganttua.api.spec.security.authenticator;

import java.util.List;

import com.garganttua.api.spec.domain.IGGAPIDomain;

public interface IGGAPIAuthenticatorInfosRegistry {

	List<GGAPIAuthenticatorInfos> getAuthenticatorInfos();

	List<IGGAPIDomain> getDomains();

	GGAPIAuthenticatorInfos getAuthenticatorInfos(String domain);

}
