package com.garganttua.api.core.security.key;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.security.ExpirationTools;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.key.EncryptionMode;
import com.garganttua.api.spec.security.key.EncryptionPaddingMode;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeyHelper {

	@SuppressWarnings("unchecked")
	public static IKeyRealm getKey(String realmName, Class<?> keyType, AuthenticatorKeyUsage keyUsage,
			boolean autoCreate, KeyAlgorithm keyAlgorithm, int keyLifeTime, TimeUnit keyLifeTimeUnit,
			String ownerUuid, String tenantId, IApiContext apiContext, EncryptionMode encryptionMode,
			EncryptionPaddingMode paddingMode, SignatureAlgorithm signatureAlgorithm) throws CoreException {

		String keyDomainName = keyType.getSimpleName().toLowerCase() + "s";
		IDomainContext<?> keyDomainContext = apiContext.getDomainContext(keyDomainName)
				.orElseThrow(() -> new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
						"Key realm domain not found: " + keyDomainName));

		ICaller caller;
		switch (keyUsage) {
		case oneForAll:
			caller = Caller.createTenantCallerWithOwnerId(tenantId,
					apiContext.getSuperTenantId() + ":" + tenantId);
			break;
		case oneForEach:
			caller = Caller.createTenantCallerWithOwnerId(tenantId, ownerUuid);
			break;
		default:
		case oneForTenant:
			caller = Caller.createTenantCallerWithOwnerId(tenantId,
					apiContext.getSuperTenantId() + ":" + tenantId);
			break;
		}

		IKeyRealm key = getRealm(caller, realmName, keyAlgorithm, autoCreate, keyDomainContext, keyLifeTime,
				keyLifeTimeUnit, encryptionMode, paddingMode, signatureAlgorithm);
		return key;
	}

	@SuppressWarnings("unchecked")
	private static IKeyRealm getRealm(ICaller caller, String realmName, KeyAlgorithm algorithm,
			boolean autoCreate, IDomainContext<?> keyDomainContext, int keyLifetime, TimeUnit keyLifetimeUnit,
			EncryptionMode encryptionMode, EncryptionPaddingMode paddingMode,
			SignatureAlgorithm signatureAlgorithm) throws CoreException {

		Filter filter = buildFilterForKeyRealm(realmName, algorithm, keyDomainContext);
		IOperationResponse response = keyDomainContext.readAll(filter, null, null, caller);

		if (response.getResponseCode() == OperationResponseCode.OK) {
			List<IKeyRealm> list = (List<IKeyRealm>) response.getResponse();
			if (list.size() > 0) {
				return list.get(0);
			} else {
				if (autoCreate) {
					return createRealm(caller, realmName, algorithm, keyDomainContext, keyLifetime,
							keyLifetimeUnit, encryptionMode, paddingMode, signatureAlgorithm);
				} else {
					throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
							"Key realm " + realmName + " not found for tenant " + caller.requestedTenantId()
									+ " and owner " + caller.ownerId());
				}
			}
		} else {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
					"Unknown error during Key realm " + realmName + " for tenant " + caller.requestedTenantId()
							+ " and owner " + caller.ownerId() + " retrieval");
		}
	}

	private static IKeyRealm createRealm(ICaller caller, String realmName, KeyAlgorithm algorithm,
			IDomainContext<?> keyDomainContext, int keyLifetime, TimeUnit keyLifetimeUnit,
			EncryptionMode encryptionMode, EncryptionPaddingMode paddingMode,
			SignatureAlgorithm signatureAlgorithm) throws CoreException {
		Date expiration = ExpirationTools.getExpirationDateFromNow(keyLifetime, keyLifetimeUnit);

		IKeyRealm entity = KeyRealmHelper.newInstance((Class<?>) keyDomainContext.getEntityClass().getType(),
				realmName, algorithm, expiration, encryptionMode, paddingMode, signatureAlgorithm);
		IOperationResponse response = keyDomainContext.createOne(entity, caller);

		if (response.getResponseCode() != OperationResponseCode.CREATED) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
					"Failed to create key realm: " + response.getResponse());
		}

		return entity;
	}

	private static Filter buildFilterForKeyRealm(String realmName, KeyAlgorithm algorithm,
			IDomainContext<?> keyDomainContext) {
		String idFieldAddress = keyDomainContext.getEntityDefinition().id().toString();
		String expirationFieldAddress = KeyRealm.getExpirationFieldAddress().toString();
		String revokedFieldAddress = KeyRealm.getRevokedFieldAddress().toString();
		String algorithmFieldAddress = KeyRealm.getAlgorithmFieldAddress().toString();
		Filter idFilter = Filter.eq(idFieldAddress, realmName);
		Filter expirationFilter = Filter.gt(expirationFieldAddress, new Date());
		Filter revokedFilter = Filter.eq(revokedFieldAddress, false);
		Filter algorithmFilter = Filter.eq(algorithmFieldAddress, algorithm);
		return Filter.and(idFilter, expirationFilter, revokedFilter, algorithmFilter);
	}

	@SuppressWarnings("unchecked")
	public static void revokeAllForOwner(String realmName, String tenantId, String ownerId, Class<?> keyType,
			IApiContext apiContext) throws CoreException {

		String keyDomainName = keyType.getSimpleName().toLowerCase() + "s";
		IDomainContext<?> keyDomainContext = apiContext.getDomainContext(keyDomainName)
				.orElseThrow(() -> new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
						"Key realm domain not found: " + keyDomainName));

		String idFieldAddress = keyDomainContext.getEntityDefinition().id().toString();
		String revokedFieldAddress = KeyRealm.getRevokedFieldAddress().toString();
		Filter revokedFilter = Filter.eq(revokedFieldAddress, false);
		Filter idFilter = Filter.eq(idFieldAddress, realmName);

		ICaller caller = Caller.createTenantCallerWithOwnerId(tenantId, ownerId);
		IOperationResponse response = keyDomainContext.readAll(Filter.and(idFilter, revokedFilter), null, null, caller);

		if (response.getResponseCode() == OperationResponseCode.OK) {
			((List<IKeyRealm>) response.getResponse()).forEach(realm -> {
				realm.revoke();
				try {
					keyDomainContext.updateOne(
							realm.getUuid(), realm, Caller.createTenantCallerWithOwnerId(tenantId, ownerId));
				} catch (Exception e) {
					log.atWarn().log("Error", e);
				}
			});
		}
	}
}
