package com.garganttua.api.core.security.authenticator;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IAuthenticatorServicesRegistry;
import com.garganttua.api.spec.service.IService;

public class AuthenticatorServicesRegistry implements IAuthenticatorServicesRegistry {

	private Map<IDomain, Pair<AuthenticatorInfos, IService>> infos;

	public AuthenticatorServicesRegistry(Map<IDomain, Pair<AuthenticatorInfos, IService>> infos) {
		this.infos = infos;
	}

	@Override
	public List<IService> getServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IDomain> getDomains() {
		return List.copyOf(this.infos.keySet());
	}

	@Override
	public Pair<AuthenticatorInfos, IService> getService(String domain) {
		Optional<Entry<IDomain, Pair<AuthenticatorInfos, IService>>> optionalDomain = this.infos.entrySet().stream().filter((entry) -> {
			return entry.getKey().getDomain().equals(domain);
		}).findFirst();
		
		if( !optionalDomain.isPresent() )
			return null;
		
		return optionalDomain.get().getValue();
	}
}
