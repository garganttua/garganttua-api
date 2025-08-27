package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.service.IServiceResponse;

@FunctionalInterface
public interface IAuthenticationService {

	IServiceResponse authenticate(IAuthenticationRequest request);

}
