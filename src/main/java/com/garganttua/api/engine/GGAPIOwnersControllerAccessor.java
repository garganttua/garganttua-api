package com.garganttua.api.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.IGGAPIOwner;
import com.garganttua.api.core.IGGAPITenant;
import com.garganttua.api.engine.accessors.IGGAPIOwnersControllerAccessor;
import com.garganttua.api.engine.registries.IGGAPIControllersRegistry;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

@Service
public class GGAPIOwnersControllerAccessor implements IGGAPIOwnersControllerAccessor {

	@Autowired
	private IGGAPIControllersRegistry controllersRegistry;

	@SuppressWarnings("unchecked")
	@Override
	public IGGAPIController<IGGAPIOwner, IGGAPIDTOObject<IGGAPIOwner>> getOwnersController() {
		for(IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> controller: this.controllersRegistry.getControllers()) {
			if( controller.getDynamicDomain().ownerEntity()==true ) {
				return (IGGAPIController<IGGAPIOwner, IGGAPIDTOObject<IGGAPIOwner>>) controller;
			}
		}
		return null;
	}

}
