package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.service.IGGAPIService;

public interface IGGAPIAuthenticationFactory {

	Object createNewAuthentication(IGGAPIAuthenticationRequest authenticationRequest, IGGAPIService authenticatorService, GGAPIAuthenticatorInfos authenticatorInfos) throws GGAPIException;

	Object createDummy(IGGAPIDomain domain) throws GGAPIException;

}
