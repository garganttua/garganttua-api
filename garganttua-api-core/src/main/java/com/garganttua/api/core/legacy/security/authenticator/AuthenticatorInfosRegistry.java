package com.garganttua.api.core.legacy.security.authenticator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IAuthenticatorInfosRegistry;

public class AuthenticatorInfosRegistry implements IAuthenticatorInfosRegistry {

	private Map<IDomain, AuthenticatorInfos> infos;

	public AuthenticatorInfosRegistry(Map<IDomain, AuthenticatorInfos> infos) {
		this.infos = infos;
	}

	@Override
	public List<AuthenticatorInfos> getAuthenticatorInfos() {
		return List.copyOf(this.infos.values());
	}

	@Override
	public List<IDomain> getDomains() {
		return List.copyOf(this.infos.keySet());
	}

	@Override
	public AuthenticatorInfos getAuthenticatorInfos(String domainName) {
		Optional<IDomain> domain = this.infos.keySet().stream().filter(d -> {
			return d.getDomain().equals(domainName);
		}).findFirst();
		
		if( !domain.isPresent() ) {
			return null;
		}
		return this.infos.get(domain.get());
	}

}
