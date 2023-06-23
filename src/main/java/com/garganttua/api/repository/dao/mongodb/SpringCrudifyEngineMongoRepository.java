package com.garganttua.api.repository.dao.mongodb;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.spec.ISpringCrudifyDomain;
import com.garganttua.api.spec.ISpringCrudifyEntity;

public class SpringCrudifyEngineMongoRepository extends SpringCrudifyMongoRepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>{
	
	public SpringCrudifyEngineMongoRepository(ISpringCrudifyDomain<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> domain, MongoTemplate mongo, String magicTenantId) {
		super(domain);
		this.mongo = mongo;
		this.magicTenantId = magicTenantId;
	}
	
}