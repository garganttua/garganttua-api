package com.garganttua.api.core.security.authenticator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorInfosRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthenticatorInfosFactory {
	
	private Set<IGGAPIDomain> domains;
	
	private Map<IGGAPIDomain, GGAPIAuthenticatorInfos> infos = new HashMap<IGGAPIDomain, GGAPIAuthenticatorInfos>();

	public GGAPIAuthenticatorInfosFactory(Set<IGGAPIDomain> domains) throws GGAPIEngineException {
		this.domains = domains;
		this.collectAuthenticators();
	}

	private void collectAuthenticators() throws GGAPIEngineException {
		log.info("*** Collecting Authenticators infos ...");
		if (this.domains == null) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "No domains");
		}
		
		List<IGGAPIDomain> authenticatorDomains = domains.stream().filter(domain -> {
			return domain.isAuthenticatorEntity();
		}).collect(Collectors.toList());
		
		authenticatorDomains.forEach(domain -> {
			try {
				GGAPIAuthenticatorInfos infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(domain.getEntityClass());
				this.infos.put(domain, infos);

				log.info("		Authenticator added [domain {}, authenticator {}]", domain.getDomain(), infos);
			} catch (GGAPIException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public IGGAPIAuthenticatorInfosRegistry getRegistry() {
		return new GGAPIAuthenticatorInfosRegistry(this.infos);
	}

}
