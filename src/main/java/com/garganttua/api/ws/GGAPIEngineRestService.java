package com.garganttua.api.ws;

import java.util.List;

import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;

public class GGAPIEngineRestService extends AbstractGGAPIService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> {

	protected IGGAPIEngine engine;

	@Override
	protected List<IGGAPIAccessRule> createCustomAuthorizations() {
		return null;
	}


	@Override
	public List<IGGAPICustomService> getCustomServices() {
		return null;
	}

}
