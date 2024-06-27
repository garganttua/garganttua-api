package com.garganttua.api.core.entity.updater;

import com.garganttua.api.core.domain.GGAPIDomain;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.updater.IGGAPIEntityUpdater;

public class GGAPIEntityUpdater implements IGGAPIEntityUpdater<Object> {
	
	private GGAPIDomain domain;
	
	@Override
	public Object update(IGGAPICaller caller, Object storedObject, Object newObject) throws GGAPIEntityUpdaterException {
		return storedObject;
	}

}
