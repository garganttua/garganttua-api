package com.garganttua.api.security.authorization.tokens.storage;

import java.util.List;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.repository.GGAPIRepository;
import com.garganttua.api.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.security.authorization.tokens.jwt.IGGAPIDBTokenKeeper;

public class GGAPITokenRepository extends GGAPIRepository implements IGGAPIDBTokenKeeper {

	@Override
	public GGAPIToken findOne(GGAPIToken example) throws GGAPIEngineException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setTenantId(example.getTenantId());
		caller.setSuperTenant(true);
		caller.setRequestedTenantId(example.getTenantId());
		
		if( example.getUuid() != null && !example.getUuid().isEmpty() ) {
			return this.getOneByUuid(GGAPIDynamicDomain.fromEntityClass(GGAPIToken.class), caller, example.getId());
		} else {
			return this.findOneByOwnerId(caller, example);
		}
	}

	@Override
	public void store(GGAPIToken token) throws GGAPIEntityException, GGAPIEngineException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setTenantId(token.getTenantId());
		caller.setSuperTenant(false);
		caller.setRequestedTenantId(token.getTenantId());
		
		GGAPIToken findOneByOwnerId = this.findOneByOwnerId(caller, token);
		if( findOneByOwnerId == null ) {
			this.save(caller, token);
		} else {
			token.setUuid(findOneByOwnerId.getUuid());
			this.update(caller, token);
		}
		
		
	}

	private GGAPIToken findOneByOwnerId(GGAPICaller caller, GGAPIToken token) throws GGAPIEngineException {

		GGAPILiteral andFilter = GGAPILiteral.eq("ownerId", token.getOwnerId()).andOperator(GGAPILiteral.eq("tenantId", token.getTenantId()));

		List<GGAPIToken> entities = this.getEntities(GGAPIDynamicDomain.fromEntityClass(GGAPIToken.class), caller, 0, 0, andFilter, null, null);
		return entities.size()>0?entities.get(0):null;
		
	}
}
