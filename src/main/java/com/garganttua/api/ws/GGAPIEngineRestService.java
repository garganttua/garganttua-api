package com.garganttua.api.ws;

import java.util.List;

import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.spec.IGGAPIEntity;

public class GGAPIEngineRestService extends AbstractGGAPIService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> {

	protected IGGAPIEngine engine;

	@Override
	protected List<IGGAPIAuthorization> createCustomAuthorizations() {
		return null;
	}


	@Override
	public List<IGGAPICustomService> getCustomServices() {
		return null;
	}

}
