package com.garganttua.api.core.entity.updater;

import com.garganttua.api.core.IGGAPICaller;

public interface IGGAPIEntityUpdater<Entity> {

	Entity update(IGGAPICaller caller, Entity storedObject, Entity newObject) throws GGAPIEntityUpdaterException;

}
