package com.garganttua.api.security.keys.managers.mongo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.garganttua.api.core.GGAPIServiceAccess;
import com.garganttua.api.core.entity.AbstractGGAPIEntity;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.security.keys.GGAPIKey;

import lombok.Getter;
import lombok.NoArgsConstructor;

@GGAPIEntity(
		domain = GGAPIKeyRealmEntity.domain, 
		dto = "com.garganttua.api.security.keys.managers.mongo.GGAPIKeyRealmDTO",
		repository = "class:com.garganttua.api.security.keys.managers.mongo.GGAPIKeyRepository",
		allow_creation = true,
		allow_read_one = true, 
		allow_read_all = true, 
		allow_update_one = false,
		allow_delete_all = true,
		allow_delete_one = true,
		allow_count = true,
		creation_access = GGAPIServiceAccess.tenant,
		read_one_access = GGAPIServiceAccess.tenant,
		read_all_access = GGAPIServiceAccess.tenant,
		delete_all_access = GGAPIServiceAccess.tenant,
		delete_one_access = GGAPIServiceAccess.tenant,
		count_access = GGAPIServiceAccess.tenant,
		creation_authority = true, 
		read_all_authority = true,
		read_one_authority = true,
		delete_all_authority = true, 
		delete_one_authority = true,
		count_authority = true,
		unicity = {"id"},
		mandatory = {"algorithm", "cipheringKey", "uncipheringKey"}
)
@NoArgsConstructor
@Getter
public class GGAPIKeyRealmEntity extends AbstractGGAPIEntity {
	
	public static final String domain = "key-realms";
	
	@JsonInclude
	private String algorithm;
	
	@JsonInclude
	private GGAPIKey cipheringKey;
	
	@JsonInclude
	private GGAPIKey uncipheringKey;
	
	GGAPIKeyRealmEntity(String uuid, String id, String algorithm, GGAPIKey cipheringKey, GGAPIKey uncipheringKey){
		super(uuid, id);
		this.algorithm = algorithm;
		this.cipheringKey = cipheringKey;
		this.uncipheringKey = uncipheringKey;
	}

}