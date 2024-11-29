package com.garganttua.api.core.security.authenticator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorInfosRegistry;

public class GGAPIAuthenticatorInfosRegistry implements IGGAPIAuthenticatorInfosRegistry {

	private Map<IGGAPIDomain, GGAPIAuthenticatorInfos> infos;

	public GGAPIAuthenticatorInfosRegistry(Map<IGGAPIDomain, GGAPIAuthenticatorInfos> infos) {
		this.infos = infos;
	}

	@Override
	public List<GGAPIAuthenticatorInfos> getAuthenticatorInfos() {
		return List.copyOf(this.infos.values());
	}

	@Override
	public List<IGGAPIDomain> getDomains() {
		return List.copyOf(this.infos.keySet());
	}

	@Override
	public GGAPIAuthenticatorInfos getAuthenticatorInfos(String domainName) {
		Optional<IGGAPIDomain> domain = this.infos.keySet().stream().filter(d -> {
			return d.getDomain().equals(domainName);
		}).findFirst();
		
		if( !domain.isPresent() ) {
			return null;
		}
		return this.infos.get(domain.get());
	}

}
