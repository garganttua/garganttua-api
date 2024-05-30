package com.garganttua.api.spec.updater;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.IGGAPICaller;

public interface IGGAPIEntityUpdater<Entity> {

	Entity update(IGGAPICaller caller, Entity storedObject, Entity newObject) throws GGAPIException;

}
