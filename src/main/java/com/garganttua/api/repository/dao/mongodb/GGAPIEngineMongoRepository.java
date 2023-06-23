package com.garganttua.api.repository.dao.mongodb;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;

public class GGAPIEngineMongoRepository extends GGAPIMongoRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>{
	
	public GGAPIEngineMongoRepository(IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> domain, MongoTemplate mongo, String magicTenantId) {
		super(domain);
		this.mongo = mongo;
		this.magicTenantId = magicTenantId;
	}
	
}