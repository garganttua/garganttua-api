package com.garganttua.api.security.keys.domain;

import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;

public class GGAPIKeyRealmDto {

	@GGFieldMappingRule(sourceFieldAddress = "algorithm")
	public String algorithm;

	public GGAPIkeyDto cipheringKey;

	public GGAPIkeyDto uncipheringKey;
	
	@GGFieldMappingRule(sourceFieldAddress = "revoked")
	@GGAPIEntityAuthorizeUpdate
	public boolean revoked;
}
