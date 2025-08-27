package com.garganttua.api.spec.updater;

import java.util.Map;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.reflection.GGObjectAddress;

public interface IEntityUpdater<Entity> {

	Entity update(ICaller caller, Object storedEntity, Object updatedEntity, Map<GGObjectAddress, String> updateAuthorizations) throws CoreException;

}
