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
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthorizationChecker;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.core.security.key.GGAPIKeyHelper;
import com.garganttua.api.core.service.GGAPIServiceResponse;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactory;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authorization.GGAPIAuthorizationInfos;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;
import com.garganttua.api.spec.sort.GGAPISort;
import com.garganttua.api.spec.sort.GGAPISortDirection;
import com.garganttua.executor.chain.GGExecutorChain;
import com.garganttua.executor.chain.GGExecutorException;
import com.garganttua.executor.chain.IGGExecutorChain;
import com.garganttua.reflection.GGObjectAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthenticationService implements IGGAPIAuthenticationService {

	public static final String AUTHORIZATION_SIGNING_KEY_REALM_NAME = "authorization-signing-key";
	
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

		this.authorizationServices = this.servicesRegistry.getServices().stream().filter(service -> {
			return this.authenticatorServices.entrySet().stream().filter(authenticatorService -> {
				return authenticatorService.getValue().getValue0().authorizationType().equals(service.getDomain().getEntity().getValue0());
			}).findFirst().isPresent();
		}).collect(Collectors.toMap(key -> key.getDomain().getEntity().getValue0(), key -> key ));
		
		this.tenantService = tenantService.get();
		this.tenantDomain = this.tenantService.getDomain();
	}

	@Override
	public IGGAPIServiceResponse authenticate(IGGAPIAuthenticationRequest authenticationRequest) {
		IGGAPIServiceResponse response = null;

		try {
			Object authentication = this.doAuthentication(authenticationRequest);
			if( authentication != null )
				response = new GGAPIServiceResponse(authentication, GGAPIServiceResponseCode.OK);
			else 
				response = new GGAPIServiceResponse(authentication, GGAPIServiceResponseCode.UNAUTHORIZED);
		} catch (GGAPIException e) {
			response = new GGAPIServiceResponse(e.getMessage(), GGAPIServiceResponseCode.UNAUTHORIZED);
		}

		return response;
	}

	private Object doAuthentication(IGGAPIAuthenticationRequest authenticationRequest)
			throws GGAPIException {
		GGExecutorChain<IGGAPIAuthenticationRequest> executorChain = new GGExecutorChain<IGGAPIAuthenticationRequest>();

		executorChain.addExecutor((request, chain) -> {
			this.createAuthenticationFromRequest(request, chain);
		});

		executorChain.addExecutor((request, chain) -> {
			this.findPrincipal(request, chain);
		});

		executorChain.addExecutor((request, chain) -> {
			this.authenticate(request, chain);
		});

		executorChain.addExecutor((request, chain) -> {
			this.findAuthorization(request, chain);
		});

		executorChain.addExecutor((request, chain) -> {
			this.createAuthorization(request, chain);
		});

		executorChain.addExecutor((request, chain) -> {
			this.storeAuthorization(request, chain);
		});

		try {
			executorChain.execute(authenticationRequest);
		} catch (GGExecutorException e) {
			GGAPIException.processException(e);
		}

		return authenticationRequest.getAuthentication();
	}

	private void storeAuthorization(IGGAPIAuthenticationRequest request, IGGExecutorChain<IGGAPIAuthenticationRequest> chain)
			throws GGExecutorException {
		try {
			GGAPIAuthenticatorInfos authenticatorInfos = GGAPIAuthenticationHelper.getAuthenticatorInfos(request.getAuthentication());
			if( authenticatorInfos != null ) {
				IGGAPIService authorizationService = this.authorizationServices.get(authenticatorInfos.authorizationType());
	
				Object authorization = GGAPIAuthenticationHelper.getAuthorization(request.getAuthentication());
	
				if (authorization != null && authorizationService != null ) {
					// store authorization
					IGGAPICaller caller = GGAPICaller.createTenantCallerWithOwnerId(request.getTenantId(), GGAPIEntityAuthorizationHelper.getOwnerId(authorization));
					
					authorizationService.createEntity(caller, authorization, new HashMap<String, String>());
				}
			}
			chain.execute(request);
		} catch (GGAPIException e) {
			throw new GGExecutorException(e);
		}
	}

	private void createAuthorization(IGGAPIAuthenticationRequest request,
			IGGExecutorChain<IGGAPIAuthenticationRequest> chain) throws GGExecutorException {
		try {
			GGAPIAuthenticatorInfos authenticatorInfos = GGAPIAuthenticationHelper
					.getAuthenticatorInfos(request.getAuthentication());
			Object foundAuthorization = GGAPIAuthenticationHelper.getAuthorization(request.getAuthentication());
			if (authenticatorInfos != null && authenticatorInfos.authorizationType() != void.class) {
				Object authorization = this.createAuthorization(request.getDomain(), request, authenticatorInfos,
						request.getAuthentication(), foundAuthorization);
				// It doesn't matter if authorization is null or not
				GGAPIAuthenticationHelper.setAuthorization(request.getAuthentication(), authorization);
			}
			chain.execute(request);
		} catch (GGAPIException e) {
			throw new GGExecutorException(e);
		}
	}
 
	@SuppressWarnings("unchecked")
	private void findAuthorization(IGGAPIAuthenticationRequest request, IGGExecutorChain<IGGAPIAuthenticationRequest> chain)
			throws GGExecutorException {
		try {
			GGAPIAuthenticatorInfos authenticatorInfos = GGAPIAuthenticationHelper.getAuthenticatorInfos(request.getAuthentication());
			if( authenticatorInfos != null && authenticatorInfos.authorizationType() != void.class) {
				GGAPIAuthorizationInfos authorizationInfos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authenticatorInfos.authorizationType());
				IGGAPIService authorizationService = this.authorizationServices.get(authenticatorInfos.authorizationType());
				Object principal = GGAPIAuthenticationHelper
						.getPrincipal(request.getAuthentication());
	
				Object authorization = null;
	
				if (authorizationService != null && GGAPIEntityAuthenticatorHelper.isAuthenticator(principal) ) {
					// Find existing authorization
					IGGAPICaller caller = GGAPICaller.createTenantCallerWithOwnerId(request.getTenantId(), GGAPIEntityHelper.getOwnerId(principal));
					IGGAPIFilter filter = this.buildFilterAuthorization(authorizationInfos);
					
					GGAPISort sort = new GGAPISort(authorizationInfos.expirationFieldAddress().toString(), GGAPISortDirection.desc);
					
					IGGAPIServiceResponse response = authorizationService.getEntities(caller, GGAPIReadOutputMode.full, null, filter, sort, new HashMap<String, String>());
	
					if( response.getResponseCode() == GGAPIServiceResponseCode.OK && ((List<Object>) response.getResponse()).size()>0) {
						authorization = ((List<Object>) response.getResponse()).get(0);
					}
					
					// It doesn't matter if authorization is null or not
					GGAPIAuthenticationHelper.setAuthorization(request.getAuthentication(), authorization);
				}
			}
			
			chain.execute(request);
		} catch (GGAPIException e) {
			throw new GGExecutorException(e);
		}
	}

	private void authenticate(IGGAPIAuthenticationRequest request, IGGExecutorChain<IGGAPIAuthenticationRequest> chain)
			throws GGExecutorException {
		try {
			GGAPIAuthenticationHelper.authenticate(request.getAuthentication());
			if (!GGAPIAuthenticationHelper.isAuthenticated(request.getAuthentication())) {
				throw new GGExecutorException(
						new GGAPISecurityException(GGAPIExceptionCode.BAD_REQUEST, "Authentication failed"));
			}
			chain.execute(request);
		} catch (GGAPIException e) {
			throw new GGExecutorException(e);
		}
	}

	private void findPrincipal(IGGAPIAuthenticationRequest request, IGGExecutorChain<IGGAPIAuthenticationRequest> chain)
			throws GGExecutorException {
		try {
			if (request.getAuthentication() != null) {
				GGAPIAuthenticationHelper.findPrincipal(request.getAuthentication());
				chain.execute(request);
			} else {
				throw new GGExecutorException(
						new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, "Authentication failed"));
			}
		} catch (GGAPIException e) {
			throw new GGExecutorException(e);
		}
	}

	private void createAuthenticationFromRequest(IGGAPIAuthenticationRequest request,
			IGGExecutorChain<IGGAPIAuthenticationRequest> chain) throws GGExecutorException {
		Object authentication;
		try {
			Pair<GGAPIAuthenticatorInfos, IGGAPIService> authenticatorService = this.authenticatorServices.get(request.getDomain());
			authentication = this.factory.createNewAuthentication((IGGAPIAuthenticationRequest) request,
					authenticatorService == null ? null : authenticatorService.getValue1(),
					authenticatorService == null ? null : authenticatorService.getValue0());
			request.setAuthentication(authentication);
			chain.execute(request);
		} catch (GGAPIException e) {
			throw new GGExecutorException(e);
		}
	}

	private Object createAuthorization(IGGAPIDomain domain, IGGAPIAuthenticationRequest request,
			GGAPIAuthenticatorInfos authenticatorInfos, Object authentication, Object foundAuthorization) throws GGAPIException {
		Object principal = GGAPIAuthenticationHelper.getPrincipal(authentication);
		if (principal == null || !GGAPIEntityAuthenticatorHelper.isAuthenticator(principal)) {
			log.atDebug().log("Authorization creation for authenticator " + request.getPrincipal()
					+ " aborded as the authentication principal is either null or not an authenticator entity");
			return null;
		}

		Object authorization = null;
		String uuid = UUID.randomUUID().toString();
		String id = request.getPrincipal();
		List<String> authorities = GGAPIAuthenticationHelper.getAuthorities(authentication);
		int lifeTime = authenticatorInfos.authorizationLifeTime();
		TimeUnit lifeTimeUnit = authenticatorInfos.authorizationLifeTimeUnit();
		String ownerUuid = GGAPIEntityHelper.getOwnerId(principal);
		String tenantId = GGAPIAuthenticationHelper.getTenantId(authentication);
		long lifeTimeInseconds = Instant.now().getEpochSecond() + lifeTimeUnit.toSeconds(lifeTime);
		Date expirationDate = Date.from(Instant.ofEpochSecond(lifeTimeInseconds));

		if (GGAPIEntityAuthorizationHelper.isSignable(authenticatorInfos.authorizationType())) {
			IGGAPIKeyRealm key = GGAPIKeyHelper.getKey(
					AUTHORIZATION_SIGNING_KEY_REALM_NAME,
					authenticatorInfos.key(), 
					authenticatorInfos.keyUsage(),
					authenticatorInfos.autoCreateKey(),
					authenticatorInfos.keyAlgorithm(),
					authenticatorInfos.keyLifeTime(),
					authenticatorInfos.keyLifeTimeUnit(),
					ownerUuid, 
					tenantId, 
					this.tenantDomain, 
					this.servicesRegistry,
					null, 
					null, 
					null);

			if( foundAuthorization != null && this.revalidateAuthorizationWithKey(foundAuthorization, key) ) {
				return foundAuthorization;
			}
			log.atDebug().log("Generating new authorization");
			authorization = GGAPIEntityAuthorizationHelper.newObject(authenticatorInfos.authorizationType(), uuid, id,
					tenantId, ownerUuid, authorities, new Date(), expirationDate, key);
		} else {
			log.atDebug().log("Generating new authorization");
			authorization = GGAPIEntityAuthorizationHelper.newObject(authenticatorInfos.authorizationType(), uuid, id,
					tenantId, ownerUuid, authorities, new Date(), expirationDate);
		}
		return authorization;
	}

	private boolean revalidateAuthorizationWithKey(Object foundAuthorization, IGGAPIKeyRealm key) {
		try {
			log.atDebug().log("Found one authorization, revalidating it with key");
			Object authToCheck = GGAPIEntityAuthorizationHelper.newObject(foundAuthorization.getClass(), GGAPIEntityAuthorizationHelper.toByteArray(foundAuthorization), key);
			GGAPIEntityAuthorizationHelper.validate(authToCheck);
		} catch (GGAPIException e) {
			log.atWarn().log("Cannot revalidate found euthorization with key");
			return false;
		}
		return true;
	}

	private IGGAPIFilter buildFilterAuthorization(GGAPIAuthorizationInfos infos) {
		GGObjectAddress expirationFieldAddress = infos.expirationFieldAddress();
		GGObjectAddress revokedFieldAddress = infos.revokedFieldAddress();
		GGAPILiteral expirationFilter = GGAPILiteral.gt(expirationFieldAddress.toString(), new Date());
		GGAPILiteral revokedFilter = GGAPILiteral.eq(revokedFieldAddress.toString(), false);
		return GGAPILiteral.and(expirationFilter, revokedFilter);
	}
}
