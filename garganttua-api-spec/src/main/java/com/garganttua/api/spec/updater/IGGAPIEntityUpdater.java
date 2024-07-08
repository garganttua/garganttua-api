package com.garganttua.api.spec.updater;

import java.util.Map;

import com.garganttua.api.spec.IGGAPICaller;

public interface IGGAPIEntityUpdater<Entity> {

	Entity update(IGGAPICaller caller, Object storedEntity, Object updatedEntity, Map<String, String> updateAuthorizations);

}
