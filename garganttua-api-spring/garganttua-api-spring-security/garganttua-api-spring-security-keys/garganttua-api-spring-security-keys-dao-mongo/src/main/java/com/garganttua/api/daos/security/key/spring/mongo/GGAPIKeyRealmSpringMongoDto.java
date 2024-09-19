package com.garganttua.api.daos.security.key.spring.mongo;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.interfaces.security.key.spring.rest.GGAPIKeyRealmSpringEntity;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.dto.annotations.GGAPIDtoTenantId;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;

@GGAPIDto(entityClass = GGAPIKeyRealmSpringEntity.class, db = "gg:SpringMongoDao")
@Document(collection = "keys")
public class GGAPIKeyRealmSpringMongoDto {
	@Id
	@GGFieldMappingRule(sourceFieldAddress = "uuid")
	private String uuid;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "id")
	private String id;
	
	@Field
	@GGAPIDtoTenantId
	@GGFieldMappingRule(sourceFieldAddress = "tenantId")
	private String tenantId;

	@Field
	@GGFieldMappingRule(sourceFieldAddress = "cipheringKey")
	private GGAPIKeySpringMongoDto cipheringKey;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "uncipheringKey")
	private GGAPIKeySpringMongoDto uncipheringKey;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "algorithm")
	private String algorithm;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "revoked")
	private boolean revoked;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "expiration")
	private Date expiration;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "ownerId")
	private String ownerId;
}
