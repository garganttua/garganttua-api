package com.garganttua.api.spec.updater;

import java.util.Map;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.ObjectAddress;

public interface IEntityUpdater {

	Object update(ICaller caller, Object storedEntity, Object updatedEntity, Map<ObjectAddress, String> updateAuthorizations) throws CoreException;

}
