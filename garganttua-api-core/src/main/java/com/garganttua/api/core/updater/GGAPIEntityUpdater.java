package com.garganttua.api.core.updater;

import java.util.Map;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.updater.IGGAPIEntityUpdater;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityUpdater implements IGGAPIEntityUpdater<Object> {

	@Override
	public Object update(IGGAPICaller caller, Object storedEntity, Object updatedEntity,
			Map<GGObjectAddress, String> updateAuthorizations) throws GGAPIException {

		try {
			final IGGObjectQuery storedEntityQuery = GGObjectQueryFactory.objectQuery(storedEntity);
			final IGGObjectQuery updatedEntityQuery = GGObjectQueryFactory.objectQuery(updatedEntity);
			if (updateAuthorizations == null) {
				throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, "Update authorizations map is null");
			}
			if( caller == null ) {
				throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, "Caller is null");
			}
			this.testIfObjectAreOfTheSameType(storedEntity, updatedEntity);

			updateAuthorizations.entrySet().parallelStream().forEach(entry -> {
				try {	
					if( this.isAuthorizedUpdate(caller, entry.getValue()) ) {
						Object updatedValue = updatedEntityQuery.getValue(entry.getKey());
						if( updatedValue != null )
							storedEntityQuery.setValue(entry.getKey(), updatedValue);
					}
				} catch (GGReflectionException e) {
					if( log.isDebugEnabled() ) {
						log.warn("Error during entity updating ", e);
					}
				}
			});

		} catch (GGReflectionException e) {
			throw new GGAPIEngineException(e);
		}

		return storedEntity;
	}

	private boolean isAuthorizedUpdate(IGGAPICaller caller, String authority) {
		if( authority != null && !authority.isEmpty() ) {
			if( caller.getAuthorities() == null ) {
				return true;
			} else {
				return caller.getAuthorities().contains(authority);
			}
		}
		return true;
	}

	private void testIfObjectAreOfTheSameType(Object storedEntity, Object entity) throws GGAPIException {
		if (!storedEntity.getClass().equals(entity.getClass())) {
			throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR,
					"Stored entity type [" + storedEntity.getClass().getSimpleName() + "] and updated entity type ["
							+ entity.getClass().getSimpleName() + "] mismatch");
		}
	}
}
