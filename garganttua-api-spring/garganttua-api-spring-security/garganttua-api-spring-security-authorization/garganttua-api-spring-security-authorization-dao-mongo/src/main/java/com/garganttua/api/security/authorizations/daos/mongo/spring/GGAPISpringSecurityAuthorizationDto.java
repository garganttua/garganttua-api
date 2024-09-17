package com.garganttua.api.security.authorizations.daos.mongo.spring;

import java.util.Collection;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.spec.dto.annotations.GGAPIDtoTenantId;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;


public abstract class GGAPISpringSecurityAuthorizationDto {
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
	@GGFieldMappingRule(sourceFieldAddress = "ownerId")
	public String ownerId;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "authorities")
	public Collection<String> authorities;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "creationDate")
	public Date creationDate;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "expirationDate")
	public Date expirationDate;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "signingKeyUuid")
	public String signingKeyUuid;
	
	@Field
	@GGFieldMappingRule(sourceFieldAddress = "revoked")
	public boolean revoked;
}
