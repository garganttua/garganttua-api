package com.garganttua.api.core.entity.updater;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.engine.GGAPIDomain;

public class GGAPIEntityUpdater implements IGGAPIEntityUpdater<Object> {
	
	private GGAPIDomain domain;
	
	@Override
	public Object update(IGGAPICaller caller, Object storedObject, Object newObject) throws GGAPIEntityUpdaterException {
		return storedObject;
	}

}
