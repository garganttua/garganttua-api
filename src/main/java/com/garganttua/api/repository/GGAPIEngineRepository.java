package com.garganttua.api.repository;

import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;

public class GGAPIEngineRepository extends GGAPIRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> {

	public GGAPIEngineRepository(IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> domain, IGGAPIDAORepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> daoRepository, String magicTenantId) {
		super(domain, magicTenantId);
		this.daoRepository = (IGGAPIDAORepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>) daoRepository;
	}

}
