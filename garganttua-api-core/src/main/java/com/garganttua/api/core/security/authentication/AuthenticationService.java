package com.garganttua.api.core.security.authentication;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.api.core.filter.Literal;
import com.garganttua.api.core.security.authorization.EntityAuthorizationHelper;
import com.garganttua.api.core.security.entity.checker.EntityAuthenticatorChecker;
import com.garganttua.api.core.security.entity.checker.EntityAuthorizationChecker;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.core.security.key.KeyHelper;
import com.garganttua.api.core.service.ServiceResponse;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.security.authentication.IAuthenticationFactory;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IAuthenticationService;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.authorization.AuthorizationInfos;
import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.api.spec.service.IService;
import com.garganttua.api.spec.service.IServiceResponse;
import com.garganttua.api.spec.service.ReadOutputMode;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.sort.Sort;
import com.garganttua.api.spec.sort.SortDirection;
import com.garganttua.executor.chain.GGExecutorChain;
import com.garganttua.executor.chain.GGExecutorException;
import com.garganttua.executor.chain.IGGExecutorChain;
import com.garganttua.reflection.GGObjectAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationService implements IAuthenticationService {

	public static final String AUTHORIZATION_SIGNING_KEY_REALM_NAME = "authorization-signing-key";

	private Map<IDomain, Pair<AuthenticatorInfos, IService>> authenticatorServices;
	private Map<Class<?>, IAuthenticationFactory> authenticationFactories;
	private Map<Class<?>, IService> authorizationServices;

	private IEngine engine;

	public AuthenticationService(
			Map<IDomain, Pair<AuthenticatorInfos, IService>> authenticatorServices,
			Map<Class<?>, IAuthenticationFactory> authenticationFactories, IEngine engine) {
		this.authenticatorServices = authenticatorServices;
		this.authenticationFactories = authenticationFactories;
		this.engine = engine;

		this.authorizationServices = this.engine.getServices().stream().filter(service -> {
			return this.authenticatorServices.entrySet().stream().filter(authenticatorService -> {
				return authenticatorService.getValue().getValue0().authorizationType()
						.equals(service.getDomainEntityClass());
			}).findFirst().isPresent();
		}).collect(Collectors.toMap(key -> key.getDomain().getEntityClass(), key -> key));
	}

	@Override
	public IServiceResponse authenticate(IAuthenticationRequest authenticationRequest) {
		IServiceResponse response = null;

		try {
			Object authentication = this.doAuthentication(authenticationRequest);
			if (authentication != null)
				response = new ServiceResponse(authentication, ServiceResponseCode.OK);
			else
				response = new ServiceResponse(authentication, ServiceResponseCode.UNAUTHORIZED);
		} catch (CoreException e) {
			response = new ServiceResponse(e.getMessage(), ServiceResponseCode.UNAUTHORIZED);
		}

		return response;
	}

	private Object doAuthentication(IAuthenticationRequest authenticationRequest)
			throws CoreException {
		GGExecutorChain<IAuthenticationRequest> executorChain = new GGExecutorChain<IAuthenticationRequest>();

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
			CoreException.processException(e);
		}

		return authenticationRequest.getAuthentication();
	}

	private void storeAuthorization(IAuthenticationRequest request,
			IGGExecutorChain<IAuthenticationRequest> chain)
			throws GGExecutorException {
		try {
			AuthenticatorInfos authenticatorInfos = AuthenticationHelper
					.getAuthenticatorInfos(request.getAuthentication());
			if (authenticatorInfos != null) {
				IService authorizationService = this.authorizationServices
						.get(authenticatorInfos.authorizationType());

				Object authorization = AuthenticationHelper.getAuthorization(request.getAuthentication());

				if (authorization != null && authorizationService != null) {
					// store authorization
					ICaller caller = Caller.createTenantCallerWithOwnerId(request.getTenantId(),
							EntityAuthorizationHelper.getOwnerId(authorization));

					authorizationService.createEntity(caller, authorization, new HashMap<String, String>());
				}
			}
			chain.execute(request);
		} catch (CoreException e) {
			throw new GGExecutorException(e);
		}
	}

	private void createAuthorization(IAuthenticationRequest request,
			IGGExecutorChain<IAuthenticationRequest> chain) throws GGExecutorException {
		try {
			AuthenticatorInfos authenticatorInfos = this
					.getAuthenticatorInfosFromAuthentication(request.getAuthentication());
			if (authenticatorInfos != null && authenticatorInfos.authorizationType() != void.class) {
				Object authorization = this.createAuthorization(request.getDomain(), request, authenticatorInfos,
						request.getAuthentication());
				// It doesn't matter if authorization is null or not
				AuthenticationHelper.setAuthorization(request.getAuthentication(), authorization);
			}
			chain.execute(request);
		} catch (CoreException e) {
			throw new GGExecutorException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void findAuthorization(IAuthenticationRequest request,
			IGGExecutorChain<IAuthenticationRequest> chain)
			throws GGExecutorException {
		try {
			AuthenticatorInfos authenticatorInfos = this
					.getAuthenticatorInfosFromAuthentication(request.getAuthentication());

			if (authenticatorInfos != null && authenticatorInfos.authorizationType() != void.class) {
				AuthorizationInfos authorizationInfos = EntityAuthorizationChecker
						.checkEntityAuthorizationClass(authenticatorInfos.authorizationType());
				IService authorizationService = this.authorizationServices
						.get(authenticatorInfos.authorizationType());

				Object authorization = null;
				Object principal = AuthenticationHelper.getPrincipal(request.getAuthentication());

				if (authorizationService != null && principal != null
						&& EntityAuthenticatorHelper.isAuthenticator(principal)) {
					// Find existing authorization
					ICaller caller = Caller.createTenantCallerWithOwnerId(request.getTenantId(),
							EntityHelper.getOwnerId(principal));
					IFilter filter = this.buildFilterAuthorization(authorizationInfos);

					Sort sort = new Sort(authorizationInfos.expirationFieldAddress().toString(),
							SortDirection.desc);

					IServiceResponse response = authorizationService.getEntities(caller, ReadOutputMode.full,
							null, filter, sort, new HashMap<String, String>());

					if (response.getResponseCode() == ServiceResponseCode.OK
							&& ((List<Object>) response.getResponse()).size() > 0) {
						authorization = ((List<Object>) response.getResponse()).get(0);
					}

					// It doesn't matter if authorization is null or not
					AuthenticationHelper.setAuthorization(request.getAuthentication(), authorization);
				}
			}

			chain.execute(request);
		} catch (CoreException e) {
			throw new GGExecutorException(e);
		}
	}

	private AuthenticatorInfos getAuthenticatorInfosFromAuthentication(Object authentication) throws CoreException {
		Object principal = AuthenticationHelper.getPrincipal(authentication);

		if (principal != null && EntityAuthenticatorHelper.isAuthenticator(principal)) {
			return EntityAuthenticatorChecker.checkEntityAuthenticator(principal);
		} else {
			return AuthenticationHelper
					.getAuthenticatorInfos(authentication);
		}
	}

	private void authenticate(IAuthenticationRequest request, IGGExecutorChain<IAuthenticationRequest> chain)
			throws GGExecutorException {
		try {
			AuthenticationHelper.authenticate(request.getAuthentication());
			if (!AuthenticationHelper.isAuthenticated(request.getAuthentication())) {
				throw new GGExecutorException(
						new SecurityException(CoreExceptionCode.BAD_REQUEST, "Authentication failed"));
			}
			String tenantId = AuthenticationHelper.getTenantId(request.getAuthentication());
			request.setTenantId(tenantId);

			chain.execute(request);
		} catch (CoreException e) {
			throw new GGExecutorException(e);
		}
	}

	private void findPrincipal(IAuthenticationRequest request, IGGExecutorChain<IAuthenticationRequest> chain)
			throws GGExecutorException {
		try {
			if (request.getAuthentication() != null) {
				AuthenticationHelper.findPrincipal(request.getAuthentication());
				chain.execute(request);
			} else {
				throw new GGExecutorException(
						new SecurityException(CoreExceptionCode.UNKNOWN_ERROR, "Authentication failed"));
			}
		} catch (CoreException e) {
			throw new GGExecutorException(e);
		}
	}

	private void createAuthenticationFromRequest(IAuthenticationRequest request,
			IGGExecutorChain<IAuthenticationRequest> chain) throws GGExecutorException {
		Object authentication;
		try {
			Pair<AuthenticatorInfos, IService> authenticatorService = this.authenticatorServices
					.get(request.getDomain());
			authentication = this.authenticationFactories.get(request.getAuthenticationType()).createNewAuthentication(
					(IAuthenticationRequest) request,
					authenticatorService == null ? null : authenticatorService.getValue1(),
					authenticatorService == null ? null : authenticatorService.getValue0());
			request.setAuthentication(authentication);
			chain.execute(request);
		} catch (CoreException e) {
			throw new GGExecutorException(e);
		}
	}

	private Object createAuthorization(IDomain domain, IAuthenticationRequest request,
			AuthenticatorInfos authenticatorInfos, Object authentication) throws CoreException {
		Object principal = AuthenticationHelper.getPrincipal(authentication);
		if (principal == null || !EntityAuthenticatorHelper.isAuthenticator(principal)) {
			log.atDebug().log("Authorization creation for authenticator " + request.getPrincipal()
					+ " aborded as the authentication principal is either null or not an authenticator entity");
			return null;
		}

		Object foundAuthorization = AuthenticationHelper.getAuthorization(request.getAuthentication());

		Object authorization = null;
		String uuid = UUID.randomUUID().toString();
		List<String> authorities = AuthenticationHelper.getAuthorities(authentication);
		int lifeTime = authenticatorInfos.authorizationLifeTime();
		TimeUnit lifeTimeUnit = authenticatorInfos.authorizationLifeTimeUnit();
		String ownerUuid = EntityHelper.getOwnerId(principal);
		String tenantId = AuthenticationHelper.getTenantId(authentication);
		long lifeTimeInseconds = Instant.now().getEpochSecond() + lifeTimeUnit.toSeconds(lifeTime);
		Date expirationDate = Date.from(Instant.ofEpochSecond(lifeTimeInseconds));

		if (EntityAuthorizationHelper.isSignable(authenticatorInfos.authorizationType())) {

			IKeyRealm key = KeyHelper.getKey(
					AUTHORIZATION_SIGNING_KEY_REALM_NAME,
					authenticatorInfos.authorizationKeyType(),
					authenticatorInfos.authorizationKeyUsage(),
					authenticatorInfos.autoCreateAuthorizationKey(),
					authenticatorInfos.authorizationKeyAlgorithm(),
					authenticatorInfos.authorizationKeyLifeTime(),
					authenticatorInfos.authorizationKeyLifeTimeUnit(),
					ownerUuid,
					tenantId,
					this.engine,
					null,
					null,
					authenticatorInfos.authorizationSignatureAlgorithm());

			if (!key.isAbleToSign()) {
				log.atDebug().log("The authenticator misses information to sign the authorization");
				throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
						"The authenticator misses information to sign the authorization");
			}

			if (foundAuthorization != null && this.revalidateAuthorizationWithKey(foundAuthorization, key)) {
				return foundAuthorization;
			}

			log.atDebug().log("Generating new authorization");
			authorization = EntityAuthorizationHelper.newObject(authenticatorInfos.authorizationType(), uuid,
					tenantId, ownerUuid, authorities, new Date(), expirationDate);
			EntityAuthorizationHelper.sign(authorization, key);

			if (EntityAuthorizationHelper.isRenewable(authenticatorInfos.authorizationType())) {
				log.atDebug().log("Generating refresh token");
				EntityAuthorizationHelper.createRefreshToken(authorization, key, expirationDate);
			}

		} else {
			log.atDebug().log("Generating new authorization");
			authorization = EntityAuthorizationHelper.newObject(authenticatorInfos.authorizationType(), uuid,
					tenantId, ownerUuid, authorities, new Date(), expirationDate);
		}
		return authorization;
	}

	private boolean revalidateAuthorizationWithKey(Object foundAuthorization, IKeyRealm key) {
		try {
			log.atDebug().log("Found one authorization, revalidating it with key");
			Object authToCheck = EntityAuthorizationHelper.newObject(foundAuthorization.getClass(),
					EntityAuthorizationHelper.toByteArray(foundAuthorization));
			EntityAuthorizationHelper.validate(authToCheck, ((Object) key));
		} catch (CoreException e) {
			log.atWarn().log("Cannot revalidate found euthorization with key");
			return false;
		}
		return true;
	}

	private IFilter buildFilterAuthorization(AuthorizationInfos infos) {
		GGObjectAddress expirationFieldAddress = infos.expirationFieldAddress();
		GGObjectAddress revokedFieldAddress = infos.revokedFieldAddress();
		Literal expirationFilter = Literal.gt(expirationFieldAddress.toString(), new Date());
		Literal revokedFilter = Literal.eq(revokedFieldAddress.toString(), false);
		return Literal.and(expirationFilter, revokedFilter);
	}
}
