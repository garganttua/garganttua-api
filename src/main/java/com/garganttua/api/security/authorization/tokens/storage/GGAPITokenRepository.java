package com.garganttua.api.security.authorization.tokens.storage;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.repository.GGAPIRepository;
import com.garganttua.api.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.security.authorization.tokens.jwt.IGGAPIDBTokenKeeper;

public class GGAPITokenRepository extends GGAPIRepository<GGAPIToken, GGAPITokenDTO> implements IGGAPIDBTokenKeeper {

	@Override
	public GGAPIToken findOne(GGAPIToken example) {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setTenantId(example.getTenantId());
		caller.setSuperTenant(true);
		caller.setRequestedTenantId(example.getTenantId());
		
		return this.getOneByUuid(caller, example.getId());
	}

	@Override
	public void store(GGAPIToken token) {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setTenantId(token.getTenantId());
		caller.setSuperTenant(false);
		caller.setRequestedTenantId(token.getTenantId());
		
		if( this.findOneByOwnerId(caller, token) == null ) {
			this.save(caller, token);
		} else {
			this.update(caller, token);
		}
		
		
	}

	private GGAPIToken findOneByOwnerId(GGAPICaller caller, GGAPIToken token) {

		GGAPILiteral ownerIdtestingFilter = GGAPILiteral.getFilterForTestingFieldEquality("ownerId", token.getOwnerId());
		GGAPILiteral tenantIdtestingFilter = GGAPILiteral.getFilterForTestingFieldEquality("tenantId", token.getTenantId());

		GGAPILiteral filter = GGAPILiteral.and(ownerIdtestingFilter, tenantIdtestingFilter);

		List<GGAPIToken> entities = this.getEntities(caller, 0, 0, filter, null, null);
		return entities.size()>0?entities.get(0):null;
		
	}
}
