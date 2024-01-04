package com.garganttua.api.engine;

import org.springframework.stereotype.Service;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.engine.accessors.IGGAPIOwnersControllerAccessor;
import com.garganttua.api.engine.registries.IGGAPIControllersRegistry;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIOwner;

@Service
public class GGAPIOwnersControllerAccessor implements IGGAPIOwnersControllerAccessor {

	public GGAPIOwnersControllerAccessor(IGGAPIDynamicDomainsRegistry dynamicDomainsRegistry,
			IGGAPIControllersRegistry controllersRegistry) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IGGAPIController<IGGAPIOwner, IGGAPIDTOObject<IGGAPIOwner>> getOwnersController() {
		// TODO Auto-generated method stub
		return null;
	}

}
