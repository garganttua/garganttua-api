package com.garganttua.api.core.dto;

import com.garganttua.core.mapper.annotations.GGFieldMappingRule;

import lombok.Data;

@Data
public class GenericTenantDto {

	@GGFieldMappingRule(sourceFieldAddress = "uuid")
	protected String uuid;
	
	@GGFieldMappingRule(sourceFieldAddress = "id")
	protected String id;

}
