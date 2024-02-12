package com.garganttua.api.engine.accessors;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIAuthenticatorAccessor {

	IGGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> getAuthenticatorController();
	
	Class<IGGAPIEntity> getAuthenticator();
	
}
