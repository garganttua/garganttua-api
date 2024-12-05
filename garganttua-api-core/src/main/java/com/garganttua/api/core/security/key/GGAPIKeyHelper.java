package com.garganttua.api.core.security.key;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.GGAPIEncryptionMode;
import com.garganttua.api.spec.security.key.GGAPIEncryptionPaddingMode;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;
import com.garganttua.reflection.GGObjectAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIKeyHelper {

	public static IGGAPIKeyRealm getKey(String realmName, Class<?> keyType, GGAPIAuthenticatorKeyUsage keyUsage, boolean autoCreate, GGAPIKeyAlgorithm keyAlgorithm, int keyLifeTime, TimeUnit keyLifeTimeUnit, String ownerUuid, String tenantId, IGGAPIDomain tenantsDomain, IGGAPIServicesRegistry registry, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode, GGAPISignatureAlgorithm signatureAlgorithm)
			throws GGAPIEngineException, GGAPIException {

		IGGAPIService keyService = registry.getService(GGAPIEntityHelper.getDomain(keyType));

		IGGAPICaller caller = null;
		switch (keyUsage) {
		case oneForAll:
			caller = GGAPICaller.createTenantCallerWithOwnerId(tenantId,
					tenantsDomain.getDomain() + ":" + tenantId);
			break;
		case oneForEach:
			caller = GGAPICaller.createTenantCallerWithOwnerId(tenantId, ownerUuid);
			break;
		default:
		case oneForTenant:
			caller = GGAPICaller.createTenantCallerWithOwnerId(tenantId,
					tenantsDomain.getDomain() + ":" + tenantId);
			break;
		}

		IGGAPIKeyRealm key = getRealm(caller, realmName, keyAlgorithm, autoCreate, keyService, keyLifeTime,
				keyLifeTimeUnit, keyService.getDomain(), encryptionMode,
				paddingMode, signatureAlgorithm);
		return key;
	}

	@SuppressWarnings("unchecked")
	private static IGGAPIKeyRealm getRealm(IGGAPICaller caller, String realmName, GGAPIKeyAlgorithm algorithm,
			boolean autoCreate, IGGAPIService keyRealmService, int keyLifetime, TimeUnit keyLifetimeUnit,
			IGGAPIDomain domain, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode, GGAPISignatureAlgorithm signatureAlgorithm) throws GGAPIException {
		IGGAPIFilter filter = buildFilterForKeyRealm(realmName, algorithm, domain);
		IGGAPIServiceResponse response = keyRealmService.getEntities(caller, GGAPIReadOutputMode.full, null, filter,
				null, new HashMap<String, String>());

		if (response.getResponseCode() == GGAPIServiceResponseCode.OK) {
			List<IGGAPIKeyRealm> list = (List<IGGAPIKeyRealm>) response.getResponse();
			if (list.size() > 0) {
				return list.get(0);
			} else {
				if (autoCreate) {
					return createRealm(caller, realmName, algorithm, keyRealmService, keyLifetime,
							keyLifetimeUnit, encryptionMode,
							paddingMode, signatureAlgorithm);
				} else {
					throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_NOT_FOUND,
							"Key realm " + realmName + " not found for tenant " + caller.getRequestedTenantId()
									+ " and owner " + caller.getOwnerId());
				}
			}
		} else {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR,
					"Unknown error during Key realm " + realmName + " for tenant " + caller.getRequestedTenantId()
							+ " and owner " + caller.getOwnerId() + " retrival");
		}
	}

	private static IGGAPIKeyRealm createRealm(IGGAPICaller caller, String realmName, GGAPIKeyAlgorithm algorithm,
			IGGAPIService keyRealmService, int keyLifetime, TimeUnit keyLifetimeUnit, GGAPIEncryptionMode encryptionMode,
			GGAPIEncryptionPaddingMode paddingMode, GGAPISignatureAlgorithm signatureAlgorithm) throws GGAPIException {
		Date expiration = Date
				.from(Instant.ofEpochSecond(Instant.now().getEpochSecond() + keyLifetimeUnit.toSeconds(keyLifetime)));

		IGGAPIKeyRealm entity = GGAPIKeyRealmHelper.newInstance(keyRealmService.getDomain().getEntity().getValue0(),
				realmName, algorithm, expiration, encryptionMode,
				paddingMode, signatureAlgorithm);
		IGGAPIServiceResponse response = keyRealmService.createEntity(caller, entity, new HashMap<String, String>());

		if (response.getResponseCode() != GGAPIServiceResponseCode.CREATED) {
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, response.getResponse().toString());
		}

		return entity;
	}
	

	private static IGGAPIFilter buildFilterForKeyRealm(String realmName, GGAPIKeyAlgorithm algorithm,
			IGGAPIDomain domain) {
		GGObjectAddress idFieldAddress = domain.getEntity().getValue1().idFieldAddress();
		GGObjectAddress expirationFieldAddress = GGAPIKeyRealm.getExpirationFieldAddress();
		GGObjectAddress revokedFieldAddress = GGAPIKeyRealm.getRevokedFieldAddress();
		GGObjectAddress algorithmFieldAddress = GGAPIKeyRealm.getAlgorithmFieldAddress();
		GGAPILiteral idFilter = GGAPILiteral.eq(idFieldAddress.toString(), realmName);
		GGAPILiteral expirationFilter = GGAPILiteral.gt(expirationFieldAddress.toString(), new Date());
		GGAPILiteral revokedFilter = GGAPILiteral.eq(revokedFieldAddress.toString(), false);
		GGAPILiteral algorithmFilter = GGAPILiteral.eq(algorithmFieldAddress.toString(), algorithm);
		return GGAPILiteral.and(idFilter, expirationFilter, revokedFilter, algorithmFilter);
	}

	public static void revokeAllForOwner(String realmName, String tenantId, String ownerId, Class<?> keyType, IGGAPIServicesRegistry registry) throws GGAPIEngineException {
		
		IGGAPIService keyService = registry.getService(GGAPIEntityHelper.getDomain(keyType));
		
		IGGAPIDomain domain = keyService.getDomain();
		
		GGObjectAddress idFieldAddress = domain.getEntity().getValue1().idFieldAddress();
		GGObjectAddress revokedFieldAddress = GGAPIKeyRealm.getRevokedFieldAddress();
		GGAPILiteral revokedFilter = GGAPILiteral.eq(revokedFieldAddress.toString(), false);
		GGAPILiteral idFilter = GGAPILiteral.eq(idFieldAddress.toString(), realmName);
		
		IGGAPIServiceResponse getEntitiesResponse = keyService.getEntities(GGAPICaller.createTenantCallerWithOwnerId(tenantId, ownerId), GGAPIReadOutputMode.full, null, GGAPILiteral.and(idFilter, revokedFilter), null, new HashMap<String, String>());
		
		if( getEntitiesResponse.getResponseCode() == GGAPIServiceResponseCode.OK ) {
			((List<IGGAPIKeyRealm>) getEntitiesResponse.getResponse()).forEach(realm -> {
				realm.revoke();
				try {
					GGAPIEntityHelper.save(realm, GGAPICaller.createTenantCallerWithOwnerId(tenantId, ownerId), new HashMap<String, String>());
				} catch (GGAPIException e) {
					log.atWarn().log("Error", e);
				}
			});
		}

		
	}

	
}
