package com.garganttua.api.security.keys.managers.mongo;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.repository.dto.AbstractGGAPIDTOObject;
import com.garganttua.api.repository.dto.IGGAPIDTOFactory;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.keys.GGAPIKey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "key-realms")
public class GGAPIKeyRealmDTO extends AbstractGGAPIDTOObject<GGAPIKeyRealmEntity>{
		
	@Field
	private String algorithm;
	
	@Field
	private GGAPIKey cipheringKey;
	
	@Field
	private GGAPIKey uncipheringKey;

	public GGAPIKeyRealmDTO(String tenantId, GGAPIKeyRealmEntity entity) {
		super(tenantId, entity);
	}

	@Override
	public void create(GGAPIKeyRealmEntity entity) {
		this.algorithm = entity.getAlgorithm();
		this.cipheringKey = entity.getCipheringKey();
		this.uncipheringKey = entity.getUncipheringKey();		
	}

	@Override
	public GGAPIKeyRealmEntity convert() {
		return new GGAPIKeyRealmEntity(this.uuid, this.id, this.algorithm, this.cipheringKey, this.uncipheringKey);
	}

	@Override
	public void update(IGGAPIDTOObject<GGAPIKeyRealmEntity> object) {
		this.algorithm = ((GGAPIKeyRealmDTO) object).algorithm;
		this.cipheringKey = ((GGAPIKeyRealmDTO) object).cipheringKey;
		this.uncipheringKey = ((GGAPIKeyRealmDTO) object).uncipheringKey;
	}

	@Override
	public IGGAPIDTOFactory<GGAPIKeyRealmEntity, GGAPIKeyRealmDTO> getFactory() {
		return new IGGAPIDTOFactory<GGAPIKeyRealmEntity, GGAPIKeyRealmDTO>() {
			
			@Override
			public GGAPIKeyRealmDTO newInstance(String tenantId, GGAPIKeyRealmEntity entity) {
				return new GGAPIKeyRealmDTO(tenantId, entity);
			}
		};
	}


}
