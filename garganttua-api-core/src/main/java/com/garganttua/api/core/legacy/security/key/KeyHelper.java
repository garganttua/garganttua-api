package com.garganttua.api.core.legacy.security.key;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.legacy.caller.Caller;
import com.garganttua.api.core.legacy.engine.EngineException;
import com.garganttua.api.core.legacy.entity.tools.EntityHelper;
import com.garganttua.api.core.filter.Literal;
import com.garganttua.api.core.legacy.security.ExpirationTools;
import com.garganttua.api.core.legacy.security.exceptions.SecurityException;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.EncryptionMode;
import com.garganttua.api.spec.security.key.EncryptionPaddingMode;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.api.spec.service.ReadOutputMode;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.service.IService;
import com.garganttua.api.spec.service.IServiceResponse;
import com.garganttua.core.reflection.ObjectAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeyHelper {

	public static IKeyRealm getKey(String realmName, Class<?> keyType, AuthenticatorKeyUsage keyUsage, boolean autoCreate, KeyAlgorithm keyAlgorithm, int keyLifeTime, TimeUnit keyLifeTimeUnit, String ownerUuid, String tenantId, IEngine engine, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode, SignatureAlgorithm signatureAlgorithm)
			throws EngineException, CoreException {

		IService keyService = engine.getService(EntityHelper.getDomain(keyType));

		ICaller caller = null;
		switch (keyUsage) {
		case oneForAll:
			caller = Caller.createTenantCallerWithOwnerId(tenantId,
					engine.getTenantDomainName() + ":" + tenantId);
			break;
		case oneForEach:
			caller = Caller.createTenantCallerWithOwnerId(tenantId, ownerUuid);
			break;
		default:
		case oneForTenant:
			caller = Caller.createTenantCallerWithOwnerId(tenantId,
					engine.getTenantDomainName() + ":" + tenantId);
			break;
		}

		IKeyRealm key = getRealm(caller, realmName, keyAlgorithm, autoCreate, keyService, keyLifeTime,
				keyLifeTimeUnit, keyService.getDomain(), encryptionMode,
				paddingMode, signatureAlgorithm);
		return key;
	}

	@SuppressWarnings("unchecked")
	private static IKeyRealm getRealm(ICaller caller, String realmName, KeyAlgorithm algorithm,
			boolean autoCreate, IService keyRealmService, int keyLifetime, TimeUnit keyLifetimeUnit,
			IDomain domain, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode, SignatureAlgorithm signatureAlgorithm) throws CoreException {
		IFilter filter = buildFilterForKeyRealm(realmName, algorithm, domain);
		IServiceResponse response = keyRealmService.getEntities(caller, ReadOutputMode.full, null, filter,
				null, new HashMap<String, String>());

		if (response.getResponseCode() == ServiceResponseCode.OK) {
			List<IKeyRealm> list = (List<IKeyRealm>) response.getResponse();
			if (list.size() > 0) {
				return list.get(0);
			} else {
				if (autoCreate) {
					return createRealm(caller, realmName, algorithm, keyRealmService, keyLifetime,
							keyLifetimeUnit, encryptionMode,
							paddingMode, signatureAlgorithm);
				} else {
					throw new SecurityException(CoreExceptionCode.ENTITY_NOT_FOUND,
							"Key realm " + realmName + " not found for tenant " + caller.getRequestedTenantId()
									+ " and owner " + caller.getOwnerId());
				}
			}
		} else {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
					"Unknown error during Key realm " + realmName + " for tenant " + caller.getRequestedTenantId()
							+ " and owner " + caller.getOwnerId() + " retrival");
		}
	}

	private static IKeyRealm createRealm(ICaller caller, String realmName, KeyAlgorithm algorithm,
			IService keyRealmService, int keyLifetime, TimeUnit keyLifetimeUnit, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode, SignatureAlgorithm signatureAlgorithm) throws CoreException {
		Date expiration = ExpirationTools.getExpirationDateFromNow(keyLifetime, keyLifetimeUnit);

		IKeyRealm entity = KeyRealmHelper.newInstance(keyRealmService.getDomain().getEntityClass(),
				realmName, algorithm, expiration, encryptionMode,
				paddingMode, signatureAlgorithm);
		IServiceResponse response = keyRealmService.createEntity(caller, entity, new HashMap<String, String>());

		if (response.getResponseCode() != ServiceResponseCode.CREATED) {
			throw new SecurityException(CoreExceptionCode.UNKNOWN_ERROR, response.getResponse().toString());
		}

		return entity;
	}
	

	private static IFilter buildFilterForKeyRealm(String realmName, KeyAlgorithm algorithm,
			IDomain domain) {
		ObjectAddress idFieldAddress = domain.getIdFieldAddress();
		ObjectAddress expirationFieldAddress = KeyRealm.getExpirationFieldAddress();
		ObjectAddress revokedFieldAddress = KeyRealm.getRevokedFieldAddress();
		ObjectAddress algorithmFieldAddress = KeyRealm.getAlgorithmFieldAddress();
		Literal idFilter = Literal.eq(idFieldAddress.toString(), realmName);
		Literal expirationFilter = Literal.gt(expirationFieldAddress.toString(), new Date());
		Literal revokedFilter = Literal.eq(revokedFieldAddress.toString(), false);
		Literal algorithmFilter = Literal.eq(algorithmFieldAddress.toString(), algorithm);
		return Literal.and(idFilter, expirationFilter, revokedFilter, algorithmFilter);
	}

	public static void revokeAllForOwner(String realmName, String tenantId, String ownerId, Class<?> keyType, IEngine engine) throws EngineException {
		IService keyService = engine.getService(EntityHelper.getDomain(keyType));
		
		IDomain domain = keyService.getDomain();
		
		ObjectAddress idFieldAddress = domain.getIdFieldAddress();
		ObjectAddress revokedFieldAddress = KeyRealm.getRevokedFieldAddress();
		Literal revokedFilter = Literal.eq(revokedFieldAddress.toString(), false);
		Literal idFilter = Literal.eq(idFieldAddress.toString(), realmName);
		
		IServiceResponse getEntitiesResponse = keyService.getEntities(Caller.createTenantCallerWithOwnerId(tenantId, ownerId), ReadOutputMode.full, null, Literal.and(idFilter, revokedFilter), null, new HashMap<String, String>());
		
		if( getEntitiesResponse.getResponseCode() == ServiceResponseCode.OK ) {
			((List<IKeyRealm>) getEntitiesResponse.getResponse()).forEach(realm -> {
				realm.revoke();
				try {
					EntityHelper.save(realm, Caller.createTenantCallerWithOwnerId(tenantId, ownerId), new HashMap<String, String>());
				} catch (CoreException e) {
					log.atWarn().log("Error", e);
				}
			});
		}
	}
}
