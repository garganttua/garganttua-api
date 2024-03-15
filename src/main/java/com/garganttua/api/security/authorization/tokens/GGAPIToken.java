package com.garganttua.api.security.authorization.tokens;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.garganttua.api.core.GGAPIServiceAccess;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.security.authorization.tokens.storage.GGAPITokenDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@GGAPIEntity(
		domain = GGAPIToken.domain, 
		dto = {GGAPITokenDTO.class},
		repository = "class:com.garganttua.api.security.authorization.tokens.storage.GGAPITokenRepository",
		allow_creation = false,
		allow_read_one = true, 
		allow_read_all = true, 
		allow_update_one = false,
		allow_delete_all = true,
		allow_delete_one = true,
		allow_count = true,
		read_one_access = GGAPIServiceAccess.tenant,
		read_all_access = GGAPIServiceAccess.tenant,
		delete_all_access = GGAPIServiceAccess.tenant,
		delete_one_access = GGAPIServiceAccess.tenant,
		count_access = GGAPIServiceAccess.tenant,
		read_all_authority = true,
		read_one_authority = true,
		delete_all_authority = true, 
		delete_one_authority = true,
		count_authority = true
)
@NoArgsConstructor
@Getter
public class GGAPIToken extends GenericGGAPIEntity implements IGGAPIToken {
	
	public static final String domain = "access-tokens";
	
	@JsonInclude
	private String ownerId;
	
	@JsonInclude
	private Date creationDate;
	
	@JsonInclude
	private Date expirationDate;
	
	@JsonInclude
	private List<String> authorities;
	
	@JsonInclude
	private byte[] token;
	
	@JsonInclude
	private String signingKeyId;

	@JsonIgnore
	private String tenantId;
	
	public GGAPIToken(String tenantId, String uuid, String ownerId, Date creationDate, Date expirationDate, List<String> authorities, byte[] token, String signingKeyId) {
		this.uuid = uuid;
		this.id = uuid;
		this.tenantId = tenantId;
		this.ownerId = ownerId;
		this.creationDate = creationDate;
		this.expirationDate = expirationDate;
		this.authorities = authorities;
		this.token = token;
		this.signingKeyId = signingKeyId;
	}

	
	@Override
	public int hashCode() {	
		return this.creationDate.hashCode() *
				this.expirationDate.hashCode() *
				this.uuid.hashCode() *
				this.ownerId.hashCode() *
				this.token.hashCode() *
				this.signingKeyId.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		GGAPIToken token = (GGAPIToken) obj;
		return this.creationDate.equals(token.creationDate) 
				&& this.expirationDate.equals(token.expirationDate)
				&& this.uuid.equals(token.getUuid())
				&& this.ownerId.equals(token.uuid)
				&& this.token.equals(token.token)
				&& this.signingKeyId.equals(token.signingKeyId);
	}
	
	@Override
	public String toString() {
		return this.uuid+":"+this.ownerId+":"+this.signingKeyId+":"+this.creationDate+":"+this.expirationDate+":"+this.token;
	}
}
