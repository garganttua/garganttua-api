/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.legacy.dto;

import com.garganttua.api.spec.dto.annotations.DtoTenantId;
import com.garganttua.core.mapper.annotations.GGFieldMappingRule;

import lombok.Data;

/**
 * 
 * @author J.Colombet
 *
 * @param <Entity>
 */
@Data
public class GenericDto {
	
	@GGFieldMappingRule(sourceFieldAddress = "uuid")
	protected String uuid;
	
	@GGFieldMappingRule(sourceFieldAddress = "id")
	protected String id;
	
	@DtoTenantId
	@GGFieldMappingRule(sourceFieldAddress = "tenantId")
	protected String tenantId;
}
