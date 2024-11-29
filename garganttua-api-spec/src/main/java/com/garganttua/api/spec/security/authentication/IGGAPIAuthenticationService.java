package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

public interface IGGAPIAuthenticationService {

	IGGAPIServiceResponse authenticate(IGGAPIDomain domain, IGGAPIAuthenticationRequest request) throws GGAPIException;

}
