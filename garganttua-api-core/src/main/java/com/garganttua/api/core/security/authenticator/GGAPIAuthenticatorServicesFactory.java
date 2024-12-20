package com.garganttua.api.core.security.authenticator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorInfosRegistry;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorServicesRegistry;
import com.garganttua.api.spec.service.IGGAPIService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthenticatorServicesFactory {

	private IGGAPIAuthenticatorInfosRegistry authenticatorInfosRegistry;
	private IGGAPIEngine engine;
	
	private Map<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>> infos = new HashMap<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>>();

	public GGAPIAuthenticatorServicesFactory(IGGAPIAuthenticatorInfosRegistry authenticatorInfosRegistry,
			IGGAPIEngine engine) {
				this.authenticatorInfosRegistry = authenticatorInfosRegistry;
				this.engine = engine;
		this.collectServices();
	}

	private void collectServices() {
		log.info("*** Collecting Authenticator Services ...");
		
		List<IGGAPIDomain> domains = this.authenticatorInfosRegistry.getDomains();
		domains.forEach(domain -> {
			GGAPIAuthenticatorInfos infos = this.authenticatorInfosRegistry.getAuthenticatorInfos(domain.getDomain());
			IGGAPIService service = this.engine.getService(domain.getDomain());
		
			log.info("		Authenticator service added [domain {}, service {}]", domain.getDomain(), service);

			this.infos.put(domain, new Pair<GGAPIAuthenticatorInfos, IGGAPIService>(infos, service));
		});
	}

	public IGGAPIAuthenticatorServicesRegistry getRegistry() {
		return new GGAPIAuthenticatorServicesRegistry(this.infos);
	}
}
