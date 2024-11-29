package com.garganttua.api.core.security.authentication;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactory;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInfosRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationServicesRegistry;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorServicesRegistry;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthenticationServicesFactory {

	private IGGAPIAuthenticationInfosRegistry authenticationInfosRegistry;
	private Map<Class<?>, IGGAPIAuthenticationService> services = new HashMap<Class<?>, IGGAPIAuthenticationService>();
	private IGGAPIAuthenticatorServicesRegistry authenticatorServicesRegistry;
	private IGGAPIAuthenticationFactoriesRegistry factoriesRegistry;
	private IGGAPIServicesRegistry servicesRegistry;

	public GGAPIAuthenticationServicesFactory(IGGAPIAuthenticationInfosRegistry authenticationInfosRegistry, IGGAPIAuthenticatorServicesRegistry authenticatorServicesRegistry, IGGAPIAuthenticationFactoriesRegistry factoriesRegistry, IGGAPIServicesRegistry servicesRegistry) throws GGAPISecurityException {
		this.authenticationInfosRegistry = authenticationInfosRegistry;
		this.authenticatorServicesRegistry = authenticatorServicesRegistry;
		this.factoriesRegistry = factoriesRegistry;
		this.servicesRegistry = servicesRegistry;
		this.createServices();
	}

	private void createServices() throws GGAPISecurityException {
		log.info("*** Creating Authentication Services ...");
		for( Class<?> authenticationType: this.authenticationInfosRegistry.getAuthentications() ) {
			GGAPIAuthenticationInfos infos = this.authenticationInfosRegistry.getAuthenticationInfos(authenticationType);

			List<IGGAPIDomain> domains = this.authenticatorServicesRegistry.getDomains();
			
			Map<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>> services = domains.stream().filter(domain -> {
				Pair<GGAPIAuthenticatorInfos, IGGAPIService> service = this.authenticatorServicesRegistry.getService(domain.getDomain());
				return service.getValue0().authenticationType().equals(authenticationType);
				
			}).map(domain -> {
				Pair<GGAPIAuthenticatorInfos, IGGAPIService> service = this.authenticatorServicesRegistry.getService(domain.getDomain());
				return new SimpleEntry<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>>(domain, service);
			}).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
			
			IGGAPIAuthenticationFactory factory = this.factoriesRegistry.getFactory(authenticationType);
			
			if( factory == null ) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Cannot find authentication factory for type "+authenticationType.getSimpleName());
			}
			
			IGGAPIAuthenticationService service = new GGAPIAuthenticationService(infos, services, factory, this.servicesRegistry);
			
			this.services.put(authenticationType, service);
			
			log.info("	Authentication Service added [Authentication {}, service {}]", authenticationType.getSimpleName(), service);
		}
	}

	public IGGAPIAuthenticationServicesRegistry getRegistry() {
		return new GGAPIAuthenticationServicesRegistry(this.services);
	}

}
