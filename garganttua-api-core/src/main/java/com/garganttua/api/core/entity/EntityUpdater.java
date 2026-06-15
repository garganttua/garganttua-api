package com.garganttua.api.core.entity;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.entity.IEntityUpdater;
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
			for (Pair<ObjectAddress, String> entry : updateAuthorizations) {
				ObjectAddress fieldAddress = entry.getValue0();
				String requiredAuthority = entry.getValue1();

				if (isAuthorized(caller, requiredAuthority)) {
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

	/**
	 * Decides whether the caller may write the field guarded by
	 * {@code requiredAuthority}. The rules mirror
	 * {@code SecurityExpressions.callerHasAuthority}:
	 *
	 * <ul>
	 *   <li>No authority required (null or empty) → allowed.</li>
	 *   <li>Otherwise the caller must carry the named authority in
	 *       {@code caller.authorities()}; a {@code null} or empty list
	 *       fails the check (the previous "null means unrestricted"
	 *       behaviour was a security hole — a freshly-built
	 *       {@code Caller.createTenantCaller} has null authorities and
	 *       must not bypass field-level gates).</li>
	 * </ul>
	 *
	 * <p>Super-tenant / super-owner status does <strong>not</strong> bypass the
	 * gate: being super grants cross-tenant / cross-owner reach, not the
	 * authority to mutate a guarded field — a super caller must still carry it.
	 */
	private static boolean isAuthorized(ICaller caller, String requiredAuthority) {
		if (requiredAuthority == null || requiredAuthority.isEmpty()) {
			return true;
		}
		List<String> callerAuthorities = caller.authorities();
		if (callerAuthorities == null) {
			return false;
		}
		return callerAuthorities.contains(requiredAuthority);
	}
}
