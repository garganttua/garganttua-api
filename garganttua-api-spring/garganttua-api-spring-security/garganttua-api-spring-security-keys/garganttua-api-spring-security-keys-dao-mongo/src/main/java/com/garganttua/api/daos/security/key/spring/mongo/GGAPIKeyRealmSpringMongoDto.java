package com.garganttua.api.daos.security.key.spring.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.interfaces.security.key.spring.rest.GGAPIKeyRealmSpringRestEntity;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.dto.annotations.GGAPIDtoTenantId;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;

@GGAPIDto(entityClass = GGAPIKeyRealmSpringRestEntity.class, db = "gg:SpringMongoDao")
@Document(collection = "keys")
public class GGAPIKeyRealmSpringMongoDto {
	@Id
	@GGFieldMappingRule(sourceFieldAddress = "uuid")
	private String uuid;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "id")
	protected String id;
	
	@Field
	@GGAPIDtoTenantId
	@GGFieldMappingRule(sourceFieldAddress = "tenantId")
	protected String tenantId;
}
