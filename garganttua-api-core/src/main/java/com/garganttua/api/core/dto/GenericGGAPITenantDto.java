package com.garganttua.api.core.dto;

import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;

import lombok.Data;

@Data
public class GenericGGAPITenantDto {

	@GGFieldMappingRule(sourceFieldAddress = "uuid")
	protected String uuid;
	
	@GGFieldMappingRule(sourceFieldAddress = "id")
	protected String id;

}
