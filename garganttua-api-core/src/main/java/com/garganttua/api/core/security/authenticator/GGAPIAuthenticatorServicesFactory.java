package com.garganttua.api.core.security.authenticator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorInfosRegistry;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorServicesRegistry;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthenticatorServicesFactory {

	private IGGAPIAuthenticatorInfosRegistry authenticatorInfosRegistry;
	private IGGAPIServicesRegistry servicesRegistry;
	
	private Map<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>> infos = new HashMap<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>>();

	public GGAPIAuthenticatorServicesFactory(IGGAPIAuthenticatorInfosRegistry authenticatorInfosRegistry,
			IGGAPIServicesRegistry servicesRegistry) {
				this.authenticatorInfosRegistry = authenticatorInfosRegistry;
				this.servicesRegistry = servicesRegistry;
		this.collectServices();
	}

	private void collectServices() {
		log.info("*** Collecting Authenticator Services ...");
		
		List<IGGAPIDomain> domains = this.authenticatorInfosRegistry.getDomains();
		domains.forEach(domain -> {
			GGAPIAuthenticatorInfos infos = this.authenticatorInfosRegistry.getAuthenticatorInfos(domain.getDomain());
			IGGAPIService service = this.servicesRegistry.getService(domain.getDomain());
		
			log.info("		Authenticator service added [domain {}, service {}]", domain.getEntity().getValue1().domain(), service);

			this.infos.put(domain, new Pair<GGAPIAuthenticatorInfos, IGGAPIService>(infos, service));
		});
	}

	public IGGAPIAuthenticatorServicesRegistry getRegistry() {
		return new GGAPIAuthenticatorServicesRegistry(this.infos);
	}
}
