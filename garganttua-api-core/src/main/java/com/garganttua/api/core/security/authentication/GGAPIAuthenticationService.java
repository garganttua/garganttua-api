package com.garganttua.api.core.security.authentication;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.core.security.key.GGAPIKeyRealm;
import com.garganttua.api.core.security.key.GGAPIKeyRealmHelper;
import com.garganttua.api.core.service.GGAPIServiceResponse;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorKeyUsage;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactory;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;
import com.garganttua.reflection.GGObjectAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//Cette classe est caca
public class GGAPIAuthenticationService implements IGGAPIAuthenticationService {

	private GGAPIAuthenticationInfos infos;
	private Map<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>> authenticatorServices;
	private IGGAPIServicesRegistry servicesRegistry;
	private IGGAPIAuthenticationFactory factory;
	private IGGAPIService tenantService;
	private IGGAPIDomain tenantDomain;
	private Map<Class<?>, IGGAPIService> authorizationServices;

	public GGAPIAuthenticationService(GGAPIAuthenticationInfos infos,
			Map<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>> authenticatorServices,
			IGGAPIAuthenticationFactory factory, IGGAPIServicesRegistry servicesRegistry) {
		this.infos = infos;
		this.authenticatorServices = authenticatorServices;
		this.factory = factory;
		this.servicesRegistry = servicesRegistry;

		Optional<IGGAPIService> tenantService = this.servicesRegistry.getServices().stream().filter(service -> {
			return service.getDomain().getEntity().getValue1().tenantEntity();
		}).findFirst();

		this.authorizationServices = this.authenticatorServices.values().stream().filter(pair -> {
			return this.servicesRegistry.getServices().stream().filter(s -> {
				return pair.getValue0().authorizationType().equals(s.getDomain().getEntity().getValue0());
			}).findFirst().isEmpty();
		}).collect(Collectors.toMap(key -> key.getValue0().authorizationType(), key -> key.getValue1()));

		this.tenantService = tenantService.get();
		this.tenantDomain = this.tenantService.getDomain();
	}

	@Override
	public IGGAPIServiceResponse authenticate(IGGAPIDomain domain, IGGAPIAuthenticationRequest request)
			throws GGAPIException {
		IGGAPIServiceResponse response = null;
		Pair<GGAPIAuthenticatorInfos, IGGAPIService> authenticatorService = this.authenticatorServices.get(domain);

		Object authentication = this.factory.createNewAuthentication(request,
				authenticatorService == null ? null : authenticatorService.getValue1(),
				authenticatorService == null ? null : authenticatorService.getValue0());

		try {
			GGAPIAuthenticationHelper.authenticate(authentication);
		} catch (GGAPIException e) {
			log.atDebug().log("Authentication failed", e);
			return new GGAPIServiceResponse(authentication, GGAPIServiceResponseCode.UNAUTHORIZED);
		}

		if (GGAPIAuthenticationHelper.isAuthenticated(authentication)) {
			log.atDebug().log("Authentication succeed");
			if (authenticatorService != null && authenticatorService.getValue0().authorizationType() != void.class) {
				log.atDebug().log("Creating authorization for authenticator " + request.getPrincipal());
				this.createAuthorization(domain, request, authenticatorService, authentication);
			}
			response = new GGAPIServiceResponse(authentication, GGAPIServiceResponseCode.OK);
		} else {
			log.atDebug().log("Authentication failed");
			response = new GGAPIServiceResponse(authentication, GGAPIServiceResponseCode.UNAUTHORIZED);
		}

		return response;
	}

	private void createAuthorization(IGGAPIDomain domain, IGGAPIAuthenticationRequest request,
			Pair<GGAPIAuthenticatorInfos, IGGAPIService> authenticatorService, Object authentication)
			throws GGAPIException {
		Object principal = GGAPIAuthenticationHelper.getPrincipal(authentication);
		if (principal == null || !GGAPIEntityAuthenticatorHelper.isAuthenticator(principal)) {
			log.atDebug().log("Authorization creation for authenticator " + request.getPrincipal()
					+ " aborded as the authentication principal is iether null or not an authenticator entity");
			return;
		}

		Object authorization = null;
		String uuid = UUID.randomUUID().toString();
		String id = request.getPrincipal();
		List<String> authorities = GGAPIAuthenticationHelper.getAuthorities(authentication);
		int lifeTime = authenticatorService.getValue0().authorizationLifeTime();
		TimeUnit lifeTimeUnit = authenticatorService.getValue0().authorizationLifeTimeUnit();
		String ownerUuid = GGAPIEntityHelper.getOwnerId(principal);
		String tenantId = GGAPIAuthenticationHelper.getTenantId(authentication);
		long lifeTimeInseconds = Instant.now().getEpochSecond() + lifeTimeUnit.toSeconds(lifeTime);
		Date expirationDate = Date.from(Instant.ofEpochSecond(lifeTimeInseconds));

		if (GGAPIEntityAuthorizationHelper.isSignable(authenticatorService.getValue0().authorizationType())) {
			IGGAPIKeyRealm key = this.getKey(authenticatorService, ownerUuid, tenantId);
			authorization = GGAPIEntityAuthorizationHelper.newObject(
					authenticatorService.getValue0().authorizationType(), uuid, id, tenantId, ownerUuid, authorities,
					new Date(), expirationDate, key);
		} else {
			authorization = GGAPIEntityAuthorizationHelper.newObject(
					authenticatorService.getValue0().authorizationType(), uuid, id, tenantId, ownerUuid, authorities,
					new Date(), expirationDate);
		}

		GGAPIAuthenticationHelper.setAuthorization(authentication, authorization);
	}

	private IGGAPIKeyRealm getKey(Pair<GGAPIAuthenticatorInfos, IGGAPIService> authenticatorService, String ownerUuid,
			String tenantId) throws GGAPIEngineException, GGAPIException {
		Class<?> keyType = authenticatorService.getValue0().key();
		GGAPIAuthenticatorKeyUsage keyUsage = authenticatorService.getValue0().keyUsage();
		boolean autoCreate = authenticatorService.getValue0().autoCreateKey();
		GGAPIKeyAlgorithm keyAlgorithm = authenticatorService.getValue0().keyAlgorithm();
		int keyLifeTime = authenticatorService.getValue0().keyLifeTime();
		TimeUnit keyLifeTimeUnit = authenticatorService.getValue0().keyLifeTimeUnit();
		IGGAPIService keyService = this.servicesRegistry.getService(GGAPIEntityHelper.getDomain(keyType));

		IGGAPICaller caller = null;
		String realmName = null;
		switch (keyUsage) {
		case oneForAll:
			caller = GGAPICaller.createTenantCallerWithOwnerId(tenantId,
					this.tenantDomain.getDomain() + ":" + tenantId);
			break;
		case oneForEach:
			caller = GGAPICaller.createTenantCallerWithOwnerId(tenantId, ownerUuid);
			break;
		default:
		case oneForTenant:
			caller = GGAPICaller.createTenantCallerWithOwnerId(tenantId,
					this.tenantDomain.getDomain() + ":" + tenantId);
			break;
		}
		realmName = "tenant-authorizations-signing-key";

		IGGAPIKeyRealm key = this.getRealm(caller, realmName, keyAlgorithm, autoCreate, keyService, keyLifeTime,
				keyLifeTimeUnit, keyService.getDomain());
		return key;
	}

	private IGGAPIKeyRealm getRealm(IGGAPICaller caller, String realmUuid, IGGAPIService keyRealmService)
			throws GGAPIException {
		IGGAPIServiceResponse response = keyRealmService.getEntity(caller, realmUuid, new HashMap<String, String>());
		if (response.getResponseCode() == GGAPIServiceResponseCode.NOT_FOUND) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_NOT_FOUND,
					"Key realm with uuid " + realmUuid + " not found");
		} else {
			IGGAPIKeyRealm realm = (IGGAPIKeyRealm) response.getResponse();

			// this line should throw exception if realm revoked or expired
			realm.getKeyForCiphering();

			return realm;
		}
	}

	@SuppressWarnings("unchecked")
	private IGGAPIKeyRealm getRealm(IGGAPICaller caller, String realmName, GGAPIKeyAlgorithm algorithm,
			boolean autoCreate, IGGAPIService keyRealmService, int keyLifetime, TimeUnit keyLifetimeUnit,
			IGGAPIDomain domain) throws GGAPIException {
		IGGAPIFilter filter = this.buildFilter(realmName, algorithm, caller.getOwnerId(), domain);
		IGGAPIServiceResponse response = keyRealmService.getEntities(caller, GGAPIReadOutputMode.full, null, filter,
				null, new HashMap<String, String>());

		if (response.getResponseCode() == GGAPIServiceResponseCode.OK) {
			List<IGGAPIKeyRealm> list = (List<IGGAPIKeyRealm>) response.getResponse();
			if (list.size() > 0) {
				return list.get(0);
			} else {
				if (autoCreate) {
					return this.createRealm(caller, realmName, algorithm, keyRealmService, keyLifetime,
							keyLifetimeUnit);
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

	private IGGAPIKeyRealm createRealm(IGGAPICaller caller, String realmName, GGAPIKeyAlgorithm algorithm,
			IGGAPIService keyRealmService, int keyLifetime, TimeUnit keyLifetimeUnit) throws GGAPIException {
		Date expiration = Date
				.from(Instant.ofEpochSecond(Instant.now().getEpochSecond() + keyLifetimeUnit.toSeconds(keyLifetime)));

		IGGAPIKeyRealm entity = GGAPIKeyRealmHelper.newInstance(keyRealmService.getDomain().getEntity().getValue0(),
				realmName, algorithm, expiration);
		IGGAPIServiceResponse response = keyRealmService.createEntity(caller, entity, new HashMap<String, String>());

		if (response.getResponseCode() != GGAPIServiceResponseCode.CREATED) {
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, response.getResponse().toString());
		}

		return entity;
	}

	private IGGAPIFilter buildFilter(String realmName, GGAPIKeyAlgorithm algorithm, String ownerId,
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

}
