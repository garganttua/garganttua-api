package com.garganttua.api.core.legacy.security.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.core.legacy.security.authentication.AuthenticationFactoryFactory;
import com.garganttua.api.core.legacy.security.authentication.AuthenticationHelper;
import com.garganttua.api.core.legacy.security.authentication.AuthenticationInfosFactory;
import com.garganttua.api.core.legacy.security.authentication.AuthenticationInterfacesFactory;
import com.garganttua.api.core.legacy.security.authentication.AuthenticationRequest;
import com.garganttua.api.core.legacy.security.authentication.AuthenticationService;
import com.garganttua.api.core.legacy.security.authenticator.AuthenticatorInfosFactory;
import com.garganttua.api.core.legacy.security.authenticator.AuthenticatorServicesFactory;
import com.garganttua.api.core.legacy.security.authorization.AuthorizationInfosFactory;
import com.garganttua.api.core.legacy.security.authorization.AuthorizationServicesFactory;
import com.garganttua.api.core.legacy.security.authorization.EntityAuthorizationHelper;
import com.garganttua.api.core.legacy.security.entity.checker.EntityAuthenticatorChecker;
import com.garganttua.api.core.legacy.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.api.core.legacy.security.exceptions.SecurityException;
import com.garganttua.api.core.legacy.service.ServiceResponse;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.IOwnerVerifier;
import com.garganttua.api.spec.security.ISecurityEngine;
import com.garganttua.api.spec.security.ITenantVerifier;
import com.garganttua.api.spec.security.authentication.IAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IAuthenticationInfosRegistry;
import com.garganttua.api.spec.security.authentication.IAuthenticationInterface;
import com.garganttua.api.spec.security.authentication.IAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IAuthenticationService;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IAuthenticatorInfosRegistry;
import com.garganttua.api.spec.security.authenticator.IAuthenticatorServicesRegistry;
import com.garganttua.api.spec.security.authorization.IAuthorizationInfosRegistry;
import com.garganttua.api.spec.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.spec.security.authorization.IAuthorizationServicesRegistry;
import com.garganttua.api.spec.service.IService;
import com.garganttua.api.spec.service.IServiceResponse;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.core.reflection.beans.IGGBeanLoader;
import com.garganttua.core.reflection.injection.IGGInjector;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityEngine implements ISecurityEngine {

	private List<String> packages;

	protected ITenantVerifier tenantVerifier = new TenantVerifier();
	protected IOwnerVerifier ownerVerifier = new OwnerVerifier();
	protected Optional<IGGInjector> injector;

	private Set<IDomain> domains;

	private IAuthenticatorInfosRegistry authenticatorInfosRegistry;
	private IAuthenticationInfosRegistry authenticationInfosRegistry;
	private IAuthorizationInfosRegistry authorizationInfosRegistry;
	@Getter
	private IAuthenticatorServicesRegistry authenticatorServicesRegistry;
	@Getter
	private IAuthorizationServicesRegistry authorizationsServicesRegistry;
	@Getter
	private IAuthenticationService authenticationService;
	private IAuthenticationFactoriesRegistry authenticationFactoryRegistry;
	@Getter
	private IAuthenticationInterfacesRegistry authenticationInterfacesRegistry;

	private IGGBeanLoader loader;

	private IEngine engine;

	protected SecurityEngine(IEngine engine, List<String> packages,
			Optional<IGGInjector> injector, IGGBeanLoader loader) {
		this.engine = engine;
		this.packages = packages;
		this.domains = this.engine.getDomains();
		this.injector = injector;
		this.loader = loader;
	}

	@Override
	public void verifyTenant(ICaller caller, Object authentication) throws CoreException {
		this.tenantVerifier.verifyTenant(caller, authentication);
	}

	@Override
	public void verifyOwner(ICaller caller, Object authentication) throws CoreException {
		this.ownerVerifier.verifyOwner(caller, authentication);
	}

	@Override
	public ISecurityEngine start() throws CoreException {
		log.info("== STARTING GARGANTTUA API SECURITY ENGINE ==");

		log.info("Injecting engine");

		log.info("Starting interfaces");
		
		for (IAuthenticationInterface interfasse : this.authenticationInterfacesRegistry.getInterfaces()) {
			log.info("*** Starting authentication interface " + interfasse.getName());
			interfasse.start();
		}
		return this;
	}

	@Override
	public ISecurityEngine stop() throws CoreException {
		log.info("== STOPPING GARGANTTUA API SECURITY ENGINE ==");
		return this;
	}

	@Override
	public ISecurityEngine reload() throws CoreException {
		log.info("== RELOADING GARGANTTUA API SECURITY ENGINE ==");
		return this;
	}

	@Override
	public ISecurityEngine flush() throws CoreException {
		log.info("== FLUSHING GARGANTTUA API SECURITY ENGINE ==");
		return this;
	}

	@Override
	public ISecurityEngine init() throws CoreException {
		log.info("============================================");
		log.info("======                                ======");
		log.info("====== Garganttua API Security Engine ======");
		log.info("======                                ======");
		log.info("============================================");
		log.info("Version: {}", this.getClass().getPackage().getImplementationVersion());
		log.info("== INITIALIZING GARGANTTUA API SECURITY ENGINE ==");

		this.authenticatorInfosRegistry = new AuthenticatorInfosFactory(this.domains).getRegistry();
		this.authenticationInfosRegistry = new AuthenticationInfosFactory(this.packages).getRegistry();
		this.authorizationInfosRegistry = new AuthorizationInfosFactory(this.packages).getRegistry();
		this.authenticatorServicesRegistry = new AuthenticatorServicesFactory(this.authenticatorInfosRegistry,
				this.engine).getRegistry();
		this.authorizationsServicesRegistry = new AuthorizationServicesFactory(this.domains,
				this.authorizationInfosRegistry, this.engine).getRegistry();
		this.authenticationFactoryRegistry = new AuthenticationFactoryFactory(this.authenticationInfosRegistry,
				this.injector).getRegistry();

		this.authenticationService = this.createAuthenticationService();

		this.authenticationInterfacesRegistry = new AuthenticationInterfacesFactory(this.loader,
				this.authenticatorInfosRegistry, this.authenticationInfosRegistry, this.authenticationFactoryRegistry,
				this.authenticationService, this.engine, this.authenticationFactoryRegistry).getRegistry();

		return this;
	}

	private IAuthenticationService createAuthenticationService() {
		log.info("*** Creating Authentication Service ...");

		Map<IDomain, Pair<AuthenticatorInfos, IService>> services = this.domains.stream()
				.filter(domain -> {
					return EntityAuthenticatorHelper.isAuthenticator(domain.getEntityClass());
				}).map(domain -> {
					Pair<AuthenticatorInfos, IService> service = this.authenticatorServicesRegistry
							.getService(domain.getDomain());
					return new SimpleEntry<IDomain, Pair<AuthenticatorInfos, IService>>(domain, service);
				}).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

		return new AuthenticationService(services, this.authenticationFactoryRegistry.getFactories(), this.engine);
	}

	private Object getAuthorization(byte[] authorizationRaw, ICaller caller) {
		Collection<Class<?>> supportedAuthorizations = caller.getDomain().getAuthorizations();
		Object authorization = null;
		for (Class<?> supportedAuthorization : supportedAuthorizations) {
			log.atDebug().log("Triing authorization type " + supportedAuthorization.getSimpleName());
			try {
				authorization = EntityAuthorizationHelper.newObject(supportedAuthorization, authorizationRaw);
			} catch (CoreException e) {
				log.atDebug().log("Error during authorization decoding ", e);
			}
		}
		return authorization;
	}

	private byte[] decodeAuthorizationFromProtocols(Object request, ICaller caller) throws CoreException {
		byte[] authorization = null;
		for (Class<?> protocolType : caller.getDomain().getAuthorizationProtocols()) {
			authorization = this.decodeAuthorizationFromProtocol(request, caller, protocolType);
			if (authorization != null) {
				break;
			}
		}
		return authorization;
	}

	private byte[] decodeAuthorizationFromProtocol(Object request, ICaller caller, Class<?> protocolType)
			throws SecurityException {
		log.atDebug().log("Triing authorization protocol " + protocolType.getSimpleName());
		byte[] authorization = null;
		IAuthorizationProtocol protocol = null;
		try {
			protocol = (IAuthorizationProtocol) protocolType.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException e) {
			throw new SecurityException(CoreExceptionCode.UNKNOWN_ERROR, "", e);
		}
		try {
			authorization = protocol.getAuthorization(request);
		} catch (CoreException e) {
			log.atDebug().log("Error during authorization protocol decoding ", e);
		}
		return authorization;
	}

	public byte[] decodeAuthorizationFromRequest(Object request, ICaller caller) throws CoreException {
		return this.decodeAuthorizationFromProtocols(request, caller);
	}

	public Object decodeRawAuthorization(byte[] authorizationRaw, ICaller caller) {
		return this.getAuthorization(authorizationRaw, caller);
	}

	@Override
	public boolean isStorableAuthorization(Object authorization) {
		Optional<IService> authorizationService = this.engine.getServices().stream().filter(service -> {
			return service.getDomain().getEntityClass().equals(authorization.getClass());
		}).findFirst();
		return authorizationService.isPresent();
	}

	@Override
	public void authenticatorEntitySecurityPreProcessing(ICaller caller, Object entity, Map<String, String> params)
			throws CoreException {
		if (EntityAuthenticatorHelper.isAuthenticator(entity.getClass())) {
			AuthenticatorInfos authenticatorInfos = EntityAuthenticatorChecker
					.checkEntityAuthenticator(entity);
			for (Class<?> authenticationType : authenticatorInfos.authenticationTypes()) {
				;
				log.atDebug()
						.log("Pre processing authenticator security on entity of type "
								+ entity.getClass().getSimpleName() + " with authentication type "
								+ authenticationType.getSimpleName());
				Object authentication = this.authenticationFactoryRegistry.getFactory(authenticationType)
						.createDummy(caller.getDomain());
				AuthenticationHelper.applyPreProcessingSecurity(authentication, caller, entity, params);
			}
		} else {
			log.atDebug().log("Cannot pre process authenticator security on entity of type "
					+ entity.getClass().getSimpleName() + " as it is not an authenticator entity");
		}
	}

	@Override
	public void authenticatorEntitySecurityPostProcessing(ICaller caller, Object entity,
			Map<String, String> params) throws CoreException {
		if (EntityAuthenticatorHelper.isAuthenticator(entity.getClass())) {
			AuthenticatorInfos authenticatorInfos = EntityAuthenticatorChecker
					.checkEntityAuthenticator(entity);
			for (Class<?> authenticationType : authenticatorInfos.authenticationTypes()) {
				;
				log.atDebug()
						.log("Post processing authenticator security on entity of type "
								+ entity.getClass().getSimpleName() + " with authentication type "
								+ authenticationType.getSimpleName());
				Object authentication = this.authenticationFactoryRegistry.getFactory(authenticationType)
						.createDummy(caller.getDomain());
				AuthenticationHelper.applyPostProcessingSecurity(authentication, caller, entity, params);
			}
		} else {
			log.atDebug().log("Cannot post process authenticator security on entity of type "
					+ entity.getClass().getSimpleName() + " as it is not an authenticator entity");
		}
	}

	@Override
	public IServiceResponse authenticate(IAuthenticationRequest request) {
		if (this.authenticationService != null) {
			return this.authenticationService.authenticate(request);
		}
		return new ServiceResponse("No authentication service", ServiceResponseCode.NOT_AVAILABLE);
	}

	public IAuthenticationRequest createAuthenticationRequestFromAuthorization(ICaller caller,
			Object authorization)
			throws CoreException {

		IDomain domain = caller.getDomain();

		if (EntityAuthenticatorHelper.isAuthenticator(authorization.getClass())) {
			AuthenticatorInfos infos = EntityAuthenticatorChecker
					.checkEntityAuthenticator(authorization);
			Optional<IDomain> authorizationsDomain = this.engine.getDomains().stream().filter(d -> {
				return d.getEntityClass().equals(authorization.getClass());
			}).findFirst();

			if (authorizationsDomain.isPresent()) {
				domain = authorizationsDomain.get();
			}

			return new AuthenticationRequest(domain,
					caller.getTenantId(),
					EntityAuthorizationHelper.getUuid(authorization), authorization,
					infos.authenticationTypes()[0]);

		} else {
			log.atWarn().log("Cannot create authentication request from authorization of type "
					+ authorization.getClass());
		}

		return null;
	}

	@Override
	public Optional<Object> getAuthorizationFromRequest(ICaller caller, Object request) throws CoreException {
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
