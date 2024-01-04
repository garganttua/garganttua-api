package com.garganttua.api.engine.accessors;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;

public interface IGGAPIAuthenticatorAccessor {

	IGGAPIController<IGGAPIAuthenticator, IGGAPIDTOObject<IGGAPIAuthenticator>> getAuthenticatorController();
	
	Class<IGGAPIAuthenticator> getAuthenticator();
	
}
