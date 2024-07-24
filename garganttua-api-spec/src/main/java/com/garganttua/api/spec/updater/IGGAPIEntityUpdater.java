package com.garganttua.api.spec.updater;

import java.util.Map;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.reflection.GGObjectAddress;

public interface IGGAPIEntityUpdater<Entity> {

	Entity update(IGGAPICaller caller, Object storedEntity, Object updatedEntity, Map<GGObjectAddress, String> updateAuthorizations) throws GGAPIException;

}
