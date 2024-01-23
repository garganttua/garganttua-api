
package com.garganttua.api.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.IGGAPITenant;
import com.garganttua.api.engine.accessors.IGGAPITenantsControllerAccessor;
import com.garganttua.api.engine.registries.IGGAPIControllersRegistry;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

@Service(value = "tenantsControllerAccessor")
public class GGAPITenantsControllerAccessor implements IGGAPITenantsControllerAccessor {
	
	@Autowired
	private IGGAPIControllersRegistry controllersRegistry;

	@SuppressWarnings("unchecked")
	@Override
	public IGGAPIController<IGGAPITenant, IGGAPIDTOObject<IGGAPITenant>> getTenantsController() {
		for(IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> controller: this.controllersRegistry.getControllers()) {
			if( controller.getDynamicDomain().tenantEntity()==true ) {
				return (IGGAPIController<IGGAPITenant, IGGAPIDTOObject<IGGAPITenant>>) controller;
			}
		}
		return null;
	}

}
