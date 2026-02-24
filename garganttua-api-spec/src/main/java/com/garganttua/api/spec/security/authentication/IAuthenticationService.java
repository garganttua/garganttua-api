package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.service.IOperationResponse;

@FunctionalInterface
public interface IAuthenticationService {

	IOperationResponse authenticate(IAuthenticationRequest request);

}
