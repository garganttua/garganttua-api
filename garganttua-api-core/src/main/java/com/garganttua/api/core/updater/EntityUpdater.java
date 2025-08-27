package com.garganttua.api.core.updater;

import java.util.Map;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.updater.IEntityUpdater;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityUpdater implements IEntityUpdater<Object> {

	@Override
	public Object update(ICaller caller, Object storedEntity, Object updatedEntity,
			Map<GGObjectAddress, String> updateAuthorizations) throws CoreException {

		try {
			final IGGObjectQuery storedEntityQuery = GGObjectQueryFactory.objectQuery(storedEntity);
			final IGGObjectQuery updatedEntityQuery = GGObjectQueryFactory.objectQuery(updatedEntity);
			if (updateAuthorizations == null) {
				throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR, "Update authorizations map is null");
			}
			if( caller == null ) {
				throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR, "Caller is null");
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
			throw new EngineException(e);
		}

		return storedEntity;
	}

	private boolean isAuthorizedUpdate(ICaller caller, String authority) {
		if( authority != null && !authority.isEmpty() ) {
			if( caller.getAuthorities() == null ) {
				return true;
			} else {
				return caller.getAuthorities().contains(authority);
			}
		}
		return true;
	}

	private void testIfObjectAreOfTheSameType(Object storedEntity, Object entity) throws CoreException {
		if (!storedEntity.getClass().equals(entity.getClass())) {
			throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR,
					"Stored entity type [" + storedEntity.getClass().getSimpleName() + "] and updated entity type ["
							+ entity.getClass().getSimpleName() + "] mismatch");
		}
	}
}
