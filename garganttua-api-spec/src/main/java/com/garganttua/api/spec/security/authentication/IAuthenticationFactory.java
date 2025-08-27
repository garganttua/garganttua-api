package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.service.IService;

public interface IAuthenticationFactory {

	Object createNewAuthentication(IAuthenticationRequest authenticationRequest, IService authenticatorService, AuthenticatorInfos authenticatorInfos) throws CoreException;

	Object createDummy(IDomain domain) throws CoreException;

}
