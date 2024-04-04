package com.garganttua.api.security.keys.managers.mongo;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.core.dto.GenericGGAPIDto;
import com.garganttua.api.core.dto.annotations.GGAPIDto;
import com.garganttua.api.core.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.core.mapper.annotations.GGAPIFieldMappingRule;
import com.garganttua.api.security.keys.GGAPIKey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "key-realms")
@GGAPIDto(entityClass = GGAPIKeyRealmEntity.class)
public class GGAPIKeyRealmDTO extends GenericGGAPIDto {
		
	@Field
	@GGAPIFieldMappingRule(sourceFieldAddress = "algorithm")
	@GGAPIEntityAuthorizeUpdate
	private String algorithm;
	
	@Field
	@GGAPIFieldMappingRule(sourceFieldAddress = "cipheringKey")
	@GGAPIEntityAuthorizeUpdate
	private GGAPIKey cipheringKey;
	
	@Field
	@GGAPIFieldMappingRule(sourceFieldAddress = "uncipheringKey")
	@GGAPIEntityAuthorizeUpdate
	private GGAPIKey uncipheringKey;

}
