package com.garganttua.api.commons.security.authentication;

import com.garganttua.api.commons.service.IOperationResponse;

@FunctionalInterface
public interface IAuthenticationService {

	IOperationResponse authenticate(IAuthenticationRequest request);

}
