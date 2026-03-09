package com.garganttua.api.core.context;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.entity.IEntityUpdater;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;

public class EntityUpdater implements IEntityUpdater{

	private static final IReflection REFLECTION = DefaultMapper.reflection();

	@Override
	public Object update(ICaller caller, Object storedEntity, Object updatedEntity,
			List<Pair<ObjectAddress, String>> updateAuthorizations) {
		if (updateAuthorizations == null || updateAuthorizations.isEmpty()) {
			return storedEntity;
		}
		if (caller == null) {
			throw new ApiException("Caller is null");
		}
		if (!storedEntity.getClass().equals(updatedEntity.getClass())) {
			throw new ApiException("Stored entity type [" + storedEntity.getClass().getSimpleName()
					+ "] and updated entity type [" + updatedEntity.getClass().getSimpleName() + "] mismatch");
		}

		try {
			List<String> callerAuthorities = caller.authorities();

			for (Pair<ObjectAddress, String> entry : updateAuthorizations) {
				ObjectAddress fieldAddress = entry.getValue0();
				String requiredAuthority = entry.getValue1();

				if (isAuthorized(callerAuthorities, requiredAuthority)) {
					String fieldName = fieldAddress.toString();
					Object updatedValue = REFLECTION.getFieldValue(updatedEntity, fieldName);
					if (updatedValue != null) {
						REFLECTION.setFieldValue(storedEntity, fieldName, updatedValue);
					}
				}
			}
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to update entity", e);
		}

		return storedEntity;
	}

	private static boolean isAuthorized(List<String> callerAuthorities, String requiredAuthority) {
		if (requiredAuthority == null || requiredAuthority.isEmpty()) {
			return true;
		}
		if (callerAuthorities == null) {
			return true;
		}
		return callerAuthorities.contains(requiredAuthority);
	}
}
