package com.garganttua.api.security.authorization.tokens.storage;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.repository.dto.AbstractGGAPIDTOObject;
import com.garganttua.api.repository.dto.IGGAPIDTOFactory;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.tokens.GGAPIToken;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "access-tokens")
public class GGAPITokenDTO extends AbstractGGAPIDTOObject<GGAPIToken>{
		
	@Field
	private String ownerId;
	
	@Field
	private Date creationDate;
	
	@Field
	private Date expirationDate;
	
	@Field
	private List<String> authorities;
	
	@Field
	private byte[] token;
	
	@Field
	private String signingKeyId;
	
	public GGAPITokenDTO(String tenantId, GGAPIToken entity) {
		super(tenantId, entity);
	}

	@Override
	public void create(GGAPIToken entity) {
		this.ownerId = entity.getOwnerId();
		this.creationDate = entity.getCreationDate();
		this.expirationDate = entity.getExpirationDate();
		this.authorities = entity.getAuthorities();
		this.token = entity.getToken();
		this.signingKeyId = entity.getSigningKeyId();
	}

	@Override
	public GGAPIToken convert() {
		return new GGAPIToken(this.tenantId, this.uuid, this.ownerId, this.creationDate, this.expirationDate, this.authorities, this.token, this.signingKeyId);
	}

	@Override
	public void update(IGGAPIDTOObject<GGAPIToken> object) {
		this.creationDate = ((GGAPITokenDTO) object).creationDate;
		this.expirationDate = ((GGAPITokenDTO) object).expirationDate;
		this.authorities = ((GGAPITokenDTO) object).authorities;
		this.token = ((GGAPITokenDTO) object).token;
		this.signingKeyId = ((GGAPITokenDTO) object).signingKeyId;
	}

}
