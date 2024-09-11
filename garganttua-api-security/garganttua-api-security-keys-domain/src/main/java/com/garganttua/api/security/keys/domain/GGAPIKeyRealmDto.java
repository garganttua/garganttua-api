package com.garganttua.api.security.keys.domain;

import com.garganttua.api.core.dto.GenericGGAPIDto;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;

public class GGAPIKeyRealmDto extends GenericGGAPIDto {

	@GGFieldMappingRule(sourceFieldAddress = "algorithm")
	protected String algorithm;

	protected GGAPIkeyDto cipheringKey;

	protected GGAPIkeyDto uncipheringKey;
	
	@GGFieldMappingRule(sourceFieldAddress = "revoked")
	@GGAPIEntityAuthorizeUpdate
	protected boolean revoked;
}
