
package com.garganttua.api.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.engine.accessors.IGGAPITenantsControllerAccessor;
import com.garganttua.api.engine.registries.IGGAPIControllersRegistry;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIEntity;

@Service(value = "tenantsControllerAccessor")
public class GGAPITenantsControllerAccessor implements IGGAPITenantsControllerAccessor {
	
	@Autowired
	private IGGAPIControllersRegistry controllersRegistry;

	@Override
	public IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getTenantsController() {
		for(IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> controller: this.controllersRegistry.getControllers()) {
			if( controller.isTenant()==true ) {
				return controller;
			}
		}
		return null;
	}

}
