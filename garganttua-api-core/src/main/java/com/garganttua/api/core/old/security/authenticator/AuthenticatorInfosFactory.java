package com.garganttua.api.core.security.authenticator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.security.entity.checker.EntityAuthenticatorChecker;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IAuthenticatorInfosRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticatorInfosFactory {
	
	private Set<IDomain> domains;
	
	private Map<IDomain, AuthenticatorInfos> infos = new HashMap<IDomain, AuthenticatorInfos>();

	public AuthenticatorInfosFactory(Set<IDomain> domains) throws EngineException {
		this.domains = domains;
		this.collectAuthenticators();
	}

	private void collectAuthenticators() throws EngineException {
		log.info("*** Collecting Authenticators infos ...");
		if (this.domains == null) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "No domains");
		}
		
		List<IDomain> authenticatorDomains = domains.stream().filter(domain -> {
			return domain.isAuthenticatorEntity();
		}).collect(Collectors.toList());
		
		authenticatorDomains.forEach(domain -> {
			try {
				AuthenticatorInfos infos = EntityAuthenticatorChecker.checkEntityAuthenticatorClass(domain.getEntityClass());
				this.infos.put(domain, infos);

				log.info("		Authenticator added [domain {}, authenticator {}]", domain.getDomain(), infos);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public IAuthenticatorInfosRegistry getRegistry() {
		return new AuthenticatorInfosRegistry(this.infos);
	}

}
