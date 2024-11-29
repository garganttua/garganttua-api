package com.garganttua.api.core.security.authenticator;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorServicesRegistry;
import com.garganttua.api.spec.service.IGGAPIService;

public class GGAPIAuthenticatorServicesRegistry implements IGGAPIAuthenticatorServicesRegistry {

	private Map<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>> infos;

	public GGAPIAuthenticatorServicesRegistry(Map<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>> infos) {
		this.infos = infos;
	}

	@Override
	public List<IGGAPIService> getServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IGGAPIDomain> getDomains() {
		return List.copyOf(this.infos.keySet());
	}

	@Override
	public Pair<GGAPIAuthenticatorInfos, IGGAPIService> getService(String domain) {
		Optional<Entry<IGGAPIDomain, Pair<GGAPIAuthenticatorInfos, IGGAPIService>>> optionalDomain = this.infos.entrySet().stream().filter((entry) -> {
			return entry.getKey().getDomain().equals(domain);
		}).findFirst();
		
		if( !optionalDomain.isPresent() )
			return null;
		
		return optionalDomain.get().getValue();
	}
}
