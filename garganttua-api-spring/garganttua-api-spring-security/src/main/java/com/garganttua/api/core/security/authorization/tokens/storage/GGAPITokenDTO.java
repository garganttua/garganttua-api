package com.garganttua.api.core.security.authorization.tokens.storage;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.core.dto.GenericGGAPIDto;
import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.tooling.objects.mapper.annotations.GGAPIFieldMappingRule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "access-tokens")
@GGAPIDto(entityClass = GGAPIToken.class)
public class GGAPITokenDTO extends GenericGGAPIDto {
		
	@Field
	@GGAPIFieldMappingRule(sourceFieldAddress = "ownerId")
	private String ownerId;
	
	@Field
	@GGAPIFieldMappingRule(sourceFieldAddress = "creationDate")
	private Date creationDate;
	
	@Field
	@GGAPIFieldMappingRule(sourceFieldAddress = "expirationDate")
	private Date expirationDate;
	
	@Field
	@GGAPIFieldMappingRule(sourceFieldAddress = "authorities")
	private List<String> authorities;
	
	@Field
	@GGAPIFieldMappingRule(sourceFieldAddress = "token")
	private byte[] token;
	
	@Field
	@GGAPIFieldMappingRule(sourceFieldAddress = "signingKeyId")
	private String signingKeyId;
	
	
}
