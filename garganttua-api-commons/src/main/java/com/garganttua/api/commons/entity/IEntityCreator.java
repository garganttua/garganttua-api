package com.garganttua.api.commons.entity;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.core.reflection.ObjectAddress;

/**
 * Applies the CREATE-time field whitelist: strips from a freshly-deserialized entity every field
 * the caller is not authorized to valorize at creation. The CREATE-time analogue of
 * {@link IEntityUpdater}.
 */
public interface IEntityCreator {

	Object create(ICaller caller, Object entity,
			List<Pair<ObjectAddress, String>> createAuthorizations) throws ApiException;

}
