package com.garganttua.api.core.security.authorization.tokens.storage;

import java.util.List;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.repository.GGAPIRepositoryException;
import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.repositories.GGAPISimpleRepository;
import com.garganttua.api.spec.dao.GGAPILiteral;
import com.garganttua.api.spec.security.IGGAPIDBTokenKeeper;

public class GGAPITokenRepository extends GGAPISimpleRepository implements IGGAPIDBTokenKeeper {

	@Override
	public GGAPIToken findOne(GGAPIToken example) throws GGAPIRepositoryException {
		GGAPICaller caller = new GGAPICaller();
		caller.setTenantId(example.getTenantId());
		caller.setSuperTenant(true);
		caller.setRequestedTenantId(example.getTenantId());
		
		if( example.getUuid() != null && !example.getUuid().isEmpty() ) {
			return (GGAPIToken) this.getOneByUuid(caller, example.getId());
		} else {
			return this.findOneByOwnerId(caller, example);
		}
	}

	@Override
	public void store(GGAPIToken token) throws GGAPIRepositoryException {
		GGAPICaller caller = new GGAPICaller();
		caller.setTenantId(token.getTenantId());
		caller.setSuperTenant(false);
		caller.setRequestedTenantId(token.getTenantId());
		
		GGAPIToken findOneByOwnerId = null;
		findOneByOwnerId = this.findOneByOwnerId(caller, token);
		if( findOneByOwnerId == null ) {
			this.save(caller, token);
		} else {
			token.setUuid(findOneByOwnerId.getUuid());
			this.update(caller, token);
		}
	}

	private GGAPIToken findOneByOwnerId(GGAPICaller caller, GGAPIToken token) throws GGAPIRepositoryException {
		GGAPILiteral andFilter = GGAPILiteral.eq("ownerId", token.getOwnerId()).andOperator(GGAPILiteral.eq("tenantId", token.getTenantId()));
		List<Object> entities = this.getEntities(caller, 0, 0, andFilter, null);
		return entities.size()>0?(GGAPIToken) entities.get(0):null;
	}
}
