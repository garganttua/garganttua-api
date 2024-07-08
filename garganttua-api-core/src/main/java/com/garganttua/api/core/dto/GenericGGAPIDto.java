/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.dto;

import com.garganttua.api.spec.dto.annotations.GGAPIDtoTenantId;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;

import lombok.Data;

/**
 * 
 * @author J.Colombet
 *
 * @param <Entity>
 */
@Data
public class GenericGGAPIDto {
	
	@GGFieldMappingRule(sourceFieldAddress = "uuid")
	protected String uuid;
	
	@GGFieldMappingRule(sourceFieldAddress = "id")
	protected String id;
	
	@GGAPIDtoTenantId
	protected String tenantId;
}
