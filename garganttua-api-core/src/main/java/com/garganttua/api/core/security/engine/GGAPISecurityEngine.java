package com.garganttua.api.core.security.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationFactoryFactory;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationHelper;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationInfosFactory;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationInterfacesFactory;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationRequest;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationService;
import com.garganttua.api.core.security.authenticator.GGAPIAuthenticatorInfosFactory;
import com.garganttua.api.core.security.authenticator.GGAPIAuthenticatorServicesFactory;
import com.garganttua.api.core.security.authorization.GGAPIAuthorization;
import com.garganttua.api.core.security.authorization.GGAPIAuthorizationInfosFactory;
import com.garganttua.api.core.security.authorization.GGAPIAuthorizationServicesFactory;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.core.service.GGAPIServiceResponse;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPIOwnerVerifier;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.security.IGGAPITenantVerifier;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInfosRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterface;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorInfosRegistry;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorServicesRegistry;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationInfosRegistry;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationProtocol;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationServicesRegistry;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPISecurityEngine implements IGGAPISecurityEngine {

	private List<String> packages;

	protected IGGAPITenantVerifier tenantVerifier = new GGAPITenantVerifier();
	protected IGGAPIOwnerVerifier ownerVerifier = new GGAPIOwnerVerifier();
	protected Optional<IGGInjector> injector;

	private Set<IGGAPIDomain> domains;

	private IGGAPIAuthenticatorInfosRegistry authenticatorInfosRegistry;
	private IGGAPIAuthenticationInfosRegistry authenticationInfosRegistry;
	private IGGAPIAuthorizationInfosRegistry authorizationInfosRegistry;
	@Getter
	private IGGAPIAuthenticatorServicesRegistry authenticatorServicesRegistry;
	@Getter
	private IGGAPIAuthorizationServicesRegistry authorizationsServicesRegistry;
	@Getter
	private IGGAPIAuthenticationService authenticationService;
	private IGGAPIAuthenticationFactoriesRegistry authenticationFactoryRegistry;
	@Getter
	private IGGAPIAuthenticationInterfacesRegistry authenticationInterfacesRegistry;

	private IGGBeanLoader loader;

	private IGGAPIEngine engine;

	protected GGAPISecurityEngine(IGGAPIEngine engine, List<String> packages,
			Optional<IGGInjector> injector, IGGBeanLoader loader) {
		this.engine = engine;
		this.packages = packages;
		this.domains = this.engine.getDomains();
		this.injector = injector;
		this.loader = loader;
	}

	@Override
	public void verifyTenant(IGGAPICaller caller, Object authentication) throws GGAPIException {
		this.tenantVerifier.verifyTenant(caller, authentication);
	}

	@Override
	public void verifyOwner(IGGAPICaller caller, Object authentication) throws GGAPIException {
		this.ownerVerifier.verifyOwner(caller, authentication);
	}

	@Override
	public IGGAPISecurityEngine start() throws GGAPIException {
		log.info("== STARTING GARGANTTUA API SECURITY ENGINE ==");

		log.info("Injecting engine");

		log.info("Starting interfaces");
		
		for (IGGAPIAuthenticationInterface interfasse : this.authenticationInterfacesRegistry.getInterfaces()) {
			log.info("*** Starting authentication interface " + interfasse.getName());
			interfasse.start();
		}
		return this;
	}

	@Override
	public IGGAPISecurityEngine stop() throws GGAPIException {
		log.info("== STOPPING GARGANTTUA API SECURITY ENGINE ==");
		return this;
	}

	@Override
	public IGGAPISecurityEngine reload() throws GGAPIException {
		log.info("== RELOADING GARGANTTUA API SECURITY ENGINE ==");
		return this;
	}

	@Override
	public IGGAPISecurityEngine flush() throws GGAPIException {
		log.info("== FLUSHING GARGANTTUA API SECURITY ENGINE ==");
		return this;
	}

	@Override
	public IGGAPISecurityEngine init() throws GGAPIException {
		log.info("============================================");
		log.info("======                                ======");
		log.info("====== Garganttua API Security Engine ======");
		log.info("======                                ======");
		log.info("============================================");
		log.info("Version: {}", this.getClass().getPackage().getImplementationVersion());
		log.info("== INITIALIZING GARGANTTUA API SECURITY ENGINE ==");

		this.authenticatorInfosRegistry = new GGAPIAuthenticatorInfosFactory(this.domains).getRegistry();
		this.authenticationInfosRegistry = new GGAPIAuthenticationInfosFactory(this.packages).getRegistry();
		this.authorizationInfosRegistry = new GGAPIAuthorizationInfosFactory(this.packages).getRegistry();
		this.authenticatorServicesRegistry = new GGAPIAuthenticatorServicesFactory(this.authenticatorInfosRegistry,
				this.engine).getRegistry();
		this.authorizationsServicesRegistry = new GGAPIAuthorizationServicesFactory(this.domains,
				this.authorizationInfosRegistry, this.engine).getRegistry();
		this.authenticationFactoryRegistry = new GGAPIAuthenticationFactoryFactory(this.authenticationInfosRegistry,
				this.injector).getRegistry();

		this.authenticationService = this.createAuthenticationService();

		this.authenticationInterfacesRegistry = new GGAPIAuthenticationInterfacesFactory(this.loader,
				this.authenticatorInfosRegistry, this.authenticationInfosRegistry, this.authenticationFactoryRegistry,
				this.authenticationService, this.engine, this.authenticationFactoryRegistry).getRegistry();

		return this;
	}

	private Object renewAuthorization(IGGAPICaller caller, Object authorization) throws GGAPIException {
		Optional<IGGAPIService> authorizationService = this.engine.getServices().stream().filter(service -> {
			return service.getDomain().getEntityClass().equals(authorization.getClass());
		}).findFirst();

		if (authorizationService.isPresent()) {
			IGGAPIService service = authorizationService.get();
			IGGAPIServiceResponse response = service.getEntity(caller, GGAPIEntityHelper.getUuid(authorization),
					new HashMap<>());

			if (response.getResponseCode() == GGAPIServiceResponseCode.OK) {
				GGAPIAuthorization storedAuthorization = (GGAPIAuthorization) response.getResponse();

			} else {
				log.atDebug().log("Cannot renew authorization as authorization cannot be found in db : "
						+ response.getResponse().toString());
			}
		} else {
			log.atDebug().log("Cannot renew authorization as no service found for type " + authorization.getClass());
		}

		return null;
	}

	private IGGAPIAuthenticationService createAuthenticationService() {
		log.info("*** Creating Authentication Service ...");

		Map<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>> services = this.domains.stream()
				.filter(domain -> {
					return GGAPIEntityAuthenticatorHelper.isAuthenticator(domain.getEntityClass());
				}).map(domain -> {
					Pair<GGAPIAuthenticatorInfos, IGGAPIService> service = this.authenticatorServicesRegistry
							.getService(domain.getDomain());
					return new SimpleEntry<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>>(domain, service);
				}).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

		return new GGAPIAuthenticationService(services, this.authenticationFactoryRegistry.getFactories(), this.engine);
	}

	private Object getAuthorization(byte[] authorizationRaw, IGGAPICaller caller) {
		Collection<Class<?>> supportedAuthorizations = caller.getDomain().getAuthorizations();
		Object authorization = null;
		for (Class<?> supportedAuthorization : supportedAuthorizations) {
			log.atDebug().log("Triing authorization type " + supportedAuthorization.getSimpleName());
			try {
				authorization = GGAPIEntityAuthorizationHelper.newObject(supportedAuthorization, authorizationRaw);
				/*
				 * if (GGAPIEntityAuthorizationHelper.isSignable(supportedAuthorization)) {
				 * 
				 * 
				 * 
				 * String ownerId = GGAPIEntityAuthorizationHelper.getOwnerId(authorization);
				 * 
				 * if (ownerId == null) {
				 * return null;
				 * }
				 * 
				 * String domainName = ownerId.split(":")[0];
				 * 
				 * Optional<IGGAPIDomain> authenticatorDomain =
				 * this.engine.getDomain(domainName);
				 * 
				 * if (authenticatorDomain.isEmpty()) {
				 * return authorization;
				 * }
				 * 
				 * if
				 * (!GGAPIEntityAuthenticatorHelper.isAuthenticator(authenticatorDomain.get().
				 * getEntityClass())) {
				 * return authorization;
				 * }
				 * 
				 * GGAPIAuthenticatorInfos authenticatorInfos = GGAPIEntityAuthenticatorChecker
				 * .checkEntityAuthenticatorClass(authenticatorDomain.get().getEntityClass());
				 * 
				 * IGGAPIKeyRealm key = GGAPIKeyHelper.getKey(
				 * GGAPIAuthenticationService.AUTHORIZATION_SIGNING_KEY_REALM_NAME,
				 * authenticatorInfos.authorizationKeyType(),
				 * authenticatorInfos.authorizationKeyUsage(),
				 * authenticatorInfos.autoCreateAuthorizationKey(),
				 * authenticatorInfos.authorizationKeyAlgorithm(),
				 * authenticatorInfos.authorizationKeyLifeTime(),
				 * authenticatorInfos.authorizationKeyLifeTimeUnit(),
				 * ownerId,
				 * caller.getTenantId(),
				 * this.engine,
				 * null,
				 * null,
				 * null);
				 * 
				 * if (key != null) {
				 * authorization =
				 * GGAPIEntityAuthorizationHelper.newObject(supportedAuthorization,
				 * authorizationRaw);
				 * }
				 * 
				 * } else {
				 * authorization =
				 * GGAPIEntityAuthorizationHelper.newObject(supportedAuthorization,
				 * authorizationRaw);
				 * }
				 */
			} catch (GGAPIException e) {
				log.atDebug().log("Error during authorization decoding ", e);
			}
		}
		return authorization;
	}

	private byte[] decodeAuthorizationFromProtocols(Object request, IGGAPICaller caller) throws GGAPISecurityException {
		byte[] authorization = null;
		for (Class<?> protocolType : caller.getDomain().getAuthorizationProtocols()) {
			authorization = this.decodeAuthorizationFromProtocol(request, caller, protocolType);
			if (authorization != null) {
				break;
			}
		}
		return authorization;
	}

	private byte[] decodeAuthorizationFromProtocol(Object request, IGGAPICaller caller, Class<?> protocolType)
			throws GGAPISecurityException {
		log.atDebug().log("Triing authorization protocol " + protocolType.getSimpleName());
		byte[] authorization = null;
		IGGAPIAuthorizationProtocol protocol = null;
		try {
			protocol = (IGGAPIAuthorizationProtocol) protocolType.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, "", e);
		}
		try {
			authorization = protocol.getAuthorization(request);
		} catch (GGAPIException e) {
			log.atDebug().log("Error during authorization protocol decoding ", e);
		}
		return authorization;
	}

	public byte[] decodeAuthorizationFromRequest(Object request, IGGAPICaller caller) throws GGAPISecurityException {
		return this.decodeAuthorizationFromProtocols(request, caller);
	}

	public Object decodeRawAuthorization(byte[] authorizationRaw, IGGAPICaller caller) {
		return this.getAuthorization(authorizationRaw, caller);
	}

	@Override
	public boolean isStorableAuthorization(Object authorization) {
		Optional<IGGAPIService> authorizationService = this.engine.getServices().stream().filter(service -> {
			return service.getDomain().getEntityClass().equals(authorization.getClass());
		}).findFirst();
		return authorizationService.isPresent();
	}

	@Override
	public void authenticatorEntitySecurityPreProcessing(IGGAPICaller caller, Object entity, Map<String, String> params)
			throws GGAPIException {
		if (GGAPIEntityAuthenticatorHelper.isAuthenticator(entity.getClass())) {
			GGAPIAuthenticatorInfos authenticatorInfos = GGAPIEntityAuthenticatorChecker
					.checkEntityAuthenticator(entity);
			for (Class<?> authenticationType : authenticatorInfos.authenticationTypes()) {
				;
				log.atDebug()
						.log("Pre processing authenticator security on entity of type "
								+ entity.getClass().getSimpleName() + " with authentication type "
								+ authenticationType.getSimpleName());
				Object authentication = this.authenticationFactoryRegistry.getFactory(authenticationType)
						.createDummy(caller.getDomain());
				GGAPIAuthenticationHelper.applyPreProcessingSecurity(authentication, caller, entity, params);
			}
		} else {
			log.atDebug().log("Cannot pre process authenticator security on entity of type "
					+ entity.getClass().getSimpleName() + " as it is not an authenticator entity");
		}
	}

	@Override
	public void authenticatorEntitySecurityPostProcessing(IGGAPICaller caller, Object entity,
			Map<String, String> params) throws GGAPIException {
		if (GGAPIEntityAuthenticatorHelper.isAuthenticator(entity.getClass())) {
			GGAPIAuthenticatorInfos authenticatorInfos = GGAPIEntityAuthenticatorChecker
					.checkEntityAuthenticator(entity);
			for (Class<?> authenticationType : authenticatorInfos.authenticationTypes()) {
				;
				log.atDebug()
						.log("Post processing authenticator security on entity of type "
								+ entity.getClass().getSimpleName() + " with authentication type "
								+ authenticationType.getSimpleName());
				Object authentication = this.authenticationFactoryRegistry.getFactory(authenticationType)
						.createDummy(caller.getDomain());
				GGAPIAuthenticationHelper.applyPostProcessingSecurity(authentication, caller, entity, params);
			}
		} else {
			log.atDebug().log("Cannot post process authenticator security on entity of type "
					+ entity.getClass().getSimpleName() + " as it is not an authenticator entity");
		}
	}

	@Override
	public IGGAPIServiceResponse authenticate(IGGAPIAuthenticationRequest request) {
		if (this.authenticationService != null) {
			return this.authenticationService.authenticate(request);
		}
		return new GGAPIServiceResponse("No authentication service", GGAPIServiceResponseCode.NOT_AVAILABLE);
	}

	public IGGAPIAuthenticationRequest createAuthenticationRequestFromAuthorization(IGGAPICaller caller,
			Object authorization)
			throws GGAPIException {

		IGGAPIDomain domain = caller.getDomain();

		if (GGAPIEntityAuthenticatorHelper.isAuthenticator(authorization.getClass())) {
			GGAPIAuthenticatorInfos infos = GGAPIEntityAuthenticatorChecker
					.checkEntityAuthenticator(authorization);
			Optional<IGGAPIDomain> authorizationsDomain = this.engine.getDomains().stream().filter(d -> {
				return d.getEntityClass().equals(authorization.getClass());
			}).findFirst();

			if (authorizationsDomain.isPresent()) {
				domain = authorizationsDomain.get();
			}

			return new GGAPIAuthenticationRequest(domain,
					caller.getTenantId(),
					GGAPIEntityAuthorizationHelper.getUuid(authorization), authorization,
					infos.authenticationTypes()[0]);

		} else {
			log.atWarn().log("Cannot create authentication request from authorization of type "
					+ authorization.getClass());
		}

		return null;
	}

	@Override
	public Optional<Object> getAuthorizationFromRequest(IGGAPICaller caller, Object request) throws GGAPIException {
		byte[] authorizationRaw = this.decodeAuthorizationFromRequest(request, caller);
		if (authorizationRaw != null && authorizationRaw.length > 0) {
			Object authorization = this.decodeRawAuthorization(authorizationRaw,
					caller);
			if (authorization != null) {
				return Optional.of(authorization);
			} else {
				log.atWarn().log("Undecodable authorization " + new String(authorizationRaw));
			}
		} else {
			log.atWarn().log("Undecodable authorization");
		}

		return Optional.empty();
	}
}
