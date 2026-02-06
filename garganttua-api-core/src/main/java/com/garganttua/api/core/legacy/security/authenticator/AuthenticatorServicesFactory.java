package com.garganttua.api.core.legacy.security.authenticator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IAuthenticatorInfosRegistry;
import com.garganttua.api.spec.security.authenticator.IAuthenticatorServicesRegistry;
import com.garganttua.api.spec.service.IService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticatorServicesFactory {

	private IAuthenticatorInfosRegistry authenticatorInfosRegistry;
	private IEngine engine;
	
	private Map<IDomain, Pair<AuthenticatorInfos, IService>> infos = new HashMap<IDomain, Pair<AuthenticatorInfos, IService>>();

	public AuthenticatorServicesFactory(IAuthenticatorInfosRegistry authenticatorInfosRegistry,
			IEngine engine) {
				this.authenticatorInfosRegistry = authenticatorInfosRegistry;
				this.engine = engine;
		this.collectServices();
	}

	private void collectServices() {
		log.info("*** Collecting Authenticator Services ...");
		
		List<IDomain> domains = this.authenticatorInfosRegistry.getDomains();
		domains.forEach(domain -> {
			AuthenticatorInfos infos = this.authenticatorInfosRegistry.getAuthenticatorInfos(domain.getDomain());
			IService service = this.engine.getService(domain.getDomain());
		
			log.info("		Authenticator service added [domain {}, service {}]", domain.getDomain(), service);

			this.infos.put(domain, new Pair<AuthenticatorInfos, IService>(infos, service));
		});
	}

	public IAuthenticatorServicesRegistry getRegistry() {
		return new AuthenticatorServicesRegistry(this.infos);
	}
}
