package com.garganttua.api.daos.security.key.spring.mongo;

import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.security.keys.domain.GGAPIKeyType;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;

public class GGAPIKeySpringMongoDto {
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "type")
	private GGAPIKeyType type;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "algorithm")
	private String algorithm;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "key")
	private byte[] key;

}
