package com.garganttua.api.security.keys.managers.mongo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.garganttua.api.core.AbstractGGAPIEntity;
import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.GGAPIEntity;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.IGGAPIEntityFactory;
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
		creation_access = GGAPICrudAccess.tenant,
		read_one_access = GGAPICrudAccess.tenant,
		read_all_access = GGAPICrudAccess.tenant,
		delete_all_access = GGAPICrudAccess.tenant,
		delete_one_access = GGAPICrudAccess.tenant,
		count_access = GGAPICrudAccess.tenant,
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

	@Override
	public IGGAPIEntityFactory<? extends IGGAPIEntity> getFactory() {
		return new IGGAPIEntityFactory<IGGAPIEntity>() {

			@Override
			public IGGAPIEntity newInstance() {
				return new GGAPIKeyRealmEntity();
			}

			@Override
			public IGGAPIEntity newInstance(String uuid) {
				return new GGAPIKeyRealmEntity(uuid, null, null, null, null);
			}
		};
	}

}
