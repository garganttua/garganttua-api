package com.garganttua.api.commons.entity;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.core.reflection.ObjectAddress;

public interface IEntityUpdater {

	Object update(ICaller caller, Object storedEntity, Object updatedEntity,
			List<Pair<ObjectAddress, String>> updateAuthorizations) throws ApiException;

}
