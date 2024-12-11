package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.service.IGGAPIServiceResponse;

@FunctionalInterface
public interface IGGAPIAuthenticationService {

	IGGAPIServiceResponse authenticate(IGGAPIAuthenticationRequest request);

}
