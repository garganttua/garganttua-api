package com.garganttua.api.security.authorization.tokens;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.garganttua.api.core.AbstractGGAPIEntity;
import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.GGAPIEntity;
import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIEntityWithTenant;

import lombok.Getter;
import lombok.NoArgsConstructor;

@GGAPIEntity(
		domain = GGAPIToken.domain, 
		dto = "com.garganttua.api.security.authorization.tokens.storage.GGAPITokenDTO",
		repository = "class:com.garganttua.api.security.authorization.tokens.storage.GGAPITokenRepository",
		allow_creation = false,
		allow_read_one = true, 
		allow_read_all = true, 
		allow_update_one = false,
		allow_delete_all = true,
		allow_delete_one = true,
		allow_count = true,
		read_one_access = GGAPICrudAccess.tenant,
		read_all_access = GGAPICrudAccess.tenant,
		delete_all_access = GGAPICrudAccess.tenant,
		delete_one_access = GGAPICrudAccess.tenant,
		count_access = GGAPICrudAccess.tenant,
		read_all_authority = true,
		read_one_authority = true,
		delete_all_authority = true, 
		delete_one_authority = true,
		count_authority = true
)
@NoArgsConstructor
@Getter
public class GGAPIToken extends AbstractGGAPIEntity implements IGGAPIToken, IGGAPIEntityWithTenant{
	
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
		super(uuid, uuid);
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

	@Override
	@JsonIgnore
	public String getTenantId() {
		return this.tenantId;
	}


	@Override
	@JsonIgnore
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;	
	}
}
