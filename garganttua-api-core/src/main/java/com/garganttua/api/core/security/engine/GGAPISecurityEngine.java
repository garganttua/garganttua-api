package com.garganttua.api.core.security.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.core.security.authentication.GGAPIAuthenticationFactoryFactory;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationHelper;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationInfosFactory;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationInterfacesFactory;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationService;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationServicesFactory;
import com.garganttua.api.core.security.authenticator.GGAPIAuthenticatorInfosFactory;
import com.garganttua.api.core.security.authenticator.GGAPIAuthenticatorServicesFactory;
import com.garganttua.api.core.security.authorization.GGAPIAuthorizationInfosFactory;
import com.garganttua.api.core.security.authorization.GGAPIAuthorizationServicesFactory;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.core.security.key.GGAPIKeyHelper;
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
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationServicesRegistry;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorInfosRegistry;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorServicesRegistry;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationInfosRegistry;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationProtocol;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationServicesRegistry;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;
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

	private IGGAPIServicesRegistry servicesRegistry;
	private Set<IGGAPIDomain> domains;

	private IGGAPIAuthenticatorInfosRegistry authenticatorInfosRegistry;
	private IGGAPIAuthenticationInfosRegistry authenticationInfosRegistry;
	private IGGAPIAuthorizationInfosRegistry authorizationInfosRegistry;
	@Getter private IGGAPIAuthenticatorServicesRegistry authenticatorServicesRegistry;
	@Getter private IGGAPIAuthorizationServicesRegistry authorizationsServicesRegistry;
	@Getter private IGGAPIAuthenticationServicesRegistry authenticationServicesRegistry;
	private IGGAPIAuthenticationFactoriesRegistry authenticationFactoryRegistry;
	@Getter private IGGAPIAuthenticationInterfacesRegistry authenticationInterfacesRegistry;

	private IGGBeanLoader loader;

	private IGGAPIEngine engine;

	protected GGAPISecurityEngine(IGGAPIEngine engine, IGGAPIServicesRegistry servicesRegistry, List<String> packages,
			Optional<IGGInjector> injector, IGGBeanLoader loader) {
		this.engine = engine;
		this.packages = packages;
		this.domains = this.engine.getDomainsRegistry().getDomains();
		this.servicesRegistry = servicesRegistry;
		this.injector = injector;
		this.loader = loader;
		this.engine.getServicesInfosRegistry();
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
				this.servicesRegistry).getRegistry();
		this.authorizationsServicesRegistry = new GGAPIAuthorizationServicesFactory(this.domains,
				this.authorizationInfosRegistry, this.servicesRegistry).getRegistry();
		this.authenticationFactoryRegistry = new GGAPIAuthenticationFactoryFactory(this.authenticationInfosRegistry,
				this.injector).getRegistry();
		this.authenticationServicesRegistry = new GGAPIAuthenticationServicesFactory(this.authenticationInfosRegistry,
				this.authenticatorServicesRegistry, this.authenticationFactoryRegistry, this.servicesRegistry).getRegistry();
		this.authenticationInterfacesRegistry = new GGAPIAuthenticationInterfacesFactory(this.loader,
				this.authenticatorInfosRegistry, this.authenticationInfosRegistry, this.authenticationFactoryRegistry,
				this.authenticationServicesRegistry, this.engine.getAccessRulesRegistry(),
				this.engine.getServicesInfosRegistry()).getRegistry();

		return this;
	}
	
	//TODO to be refactored
	private Object getAuthorization(byte[] authorizationRaw, IGGAPICaller caller) {
		Class<?>[] supportedAuthorizations = caller.getDomain().getSecurity().getAuthorizations();
		Object authorization = null;
		for(Class<?> supportedAuthorization: supportedAuthorizations ) {
			log.atDebug().log("Triing authorization type "+supportedAuthorization.getSimpleName() );
			try {
				if( GGAPIEntityAuthorizationHelper.isSignable(supportedAuthorization) ) {

					authorization = GGAPIEntityAuthorizationHelper.newObject(supportedAuthorization, authorizationRaw, null);
					
					String ownerId = GGAPIEntityAuthorizationHelper.getOwnerId(authorization);
					
					if( ownerId == null ) {
						return null;
					} 
					
					String domainName = ownerId.split(":")[0];
					
					Optional<IGGAPIDomain> authenticatorDomain = this.domains.stream().filter( d -> {
						return d.getDomain().equals(domainName);
					}).findFirst();
					
					if( authenticatorDomain.isEmpty() ) {
						return authorization;
					}
					
					if( !GGAPIEntityAuthenticatorHelper.isAuthenticator(authenticatorDomain.get().getEntity().getValue0()) ) {
						return authorization;
					}
					
					GGAPIAuthenticatorInfos authenticatorInfos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(authenticatorDomain.get().getEntity().getValue0());

					Optional<IGGAPIDomain> tenantDomain = this.domains.stream().filter(d -> {
						return d.getEntity().getValue1().tenantEntity();
					}).findFirst();
					
					IGGAPIKeyRealm key = GGAPIKeyHelper.getKey(
							GGAPIAuthenticationService.AUTHORIZATION_SIGNING_KEY_REALM_NAME,
							authenticatorInfos.key(), 
							authenticatorInfos.keyUsage(),
							authenticatorInfos.autoCreateKey(),
							authenticatorInfos.keyAlgorithm(),
							authenticatorInfos.keyLifeTime(),
							authenticatorInfos.keyLifeTimeUnit(),
							ownerId, 
							caller.getTenantId(), 
							tenantDomain.get(), 
							this.servicesRegistry,
							null,
							null,
							null);
					
					if( key != null ) {
						authorization = GGAPIEntityAuthorizationHelper.newObject(supportedAuthorization, authorizationRaw, key);
					} 

				} else {
					authorization = GGAPIEntityAuthorizationHelper.newObject(supportedAuthorization, authorizationRaw);
				}
			} catch (GGAPIException e) {
				log.atDebug().log("Error during authorization decoding ", e);
			}
		}
		return authorization; 
	}

	private byte[] decodeAuthorizationFromProtocols(Object request, IGGAPICaller caller) throws GGAPISecurityException {
		byte[] authorization = null;
		for( Class<?> protocolType: caller.getDomain().getSecurity().getAuthorizationProtocols() ) {
			authorization = this.decodeAuthorizationFromProtocol(request, caller, protocolType);
			if( authorization != null ) {
				break;
			}
		}
		return authorization;
	}

	private byte[] decodeAuthorizationFromProtocol(Object request, IGGAPICaller caller, Class<?> protocolType) throws GGAPISecurityException {
		log.atDebug().log("Triing authorization protocol "+protocolType.getSimpleName() );
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
		} catch(GGAPIException e) {
			log.atDebug().log("Error during authorization protocol decoding ", e );
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
		Optional<IGGAPIService> authorizationService = this.servicesRegistry.getServices().stream().filter(service -> {
			return service.getDomain().getEntity().getValue0().equals(authorization.getClass());
		}).findFirst();
		return authorizationService.isPresent();
	}

	@Override
	public void applySecurityOnAuthenticatorEntity(IGGAPICaller caller, Object entity, Map<String, String> params) throws GGAPIException {
		if( GGAPIEntityAuthenticatorHelper.isAuthenticator(entity.getClass()) ) {
			GGAPIAuthenticatorInfos authenticatorInfos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticator(entity);
			log.atDebug().log("Appliing authenticator security on entity of type "+entity.getClass().getSimpleName()+" with authentication type "+authenticatorInfos.authenticationType().getSimpleName());
			Object authentication = this.authenticationFactoryRegistry.getFactory(authenticatorInfos.authenticationType()).createDummy();
			GGAPIAuthenticationHelper.applySecurity(authentication, caller, entity, params);			
		} else {
			log.atDebug().log("Cannot apply authenticator security on entity of type "+entity.getClass().getSimpleName()+" as it is not an authenticator entity");
		}
	}

}
