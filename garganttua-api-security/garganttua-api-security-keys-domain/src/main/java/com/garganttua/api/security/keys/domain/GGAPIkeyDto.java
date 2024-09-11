package com.garganttua.api.security.keys.domain;

import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;

public class GGAPIkeyDto {

	@GGFieldMappingRule(sourceFieldAddress = "uuid")
	private String uuid;

	@GGFieldMappingRule(sourceFieldAddress = "key")
	private byte[] key;
	
}
