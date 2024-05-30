/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.spec.dto.annotations.GGAPIDtoTenantId;
import com.garganttua.tooling.objects.mapper.annotations.GGAPIFieldMappingRule;

import lombok.Data;

/**
 * 
 * @author J.Colombet
 *
 * @param <Entity>
 */
@Data
public class GenericGGAPIDto {
	
	@Id
	@Indexed(unique=true)
	@GGAPIFieldMappingRule(sourceFieldAddress = "uuid")
	protected String uuid;
	
	@Field
	@GGAPIFieldMappingRule(sourceFieldAddress = "id")
	protected String id;
	
	@Field
	@GGAPIDtoTenantId
	@GGAPIFieldMappingRule(sourceFieldAddress = "tenantId")
	protected String tenantId;

}
