package com.garganttua.api.security.authorization.token;

import java.util.Date;

import com.garganttua.api.security.authentication.IGGAPIAuthenticator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GGAPIToken implements IGGAPIToken {
	
	private String uuid;
	
	private String ownerId;
	
	private String tenantId;
	
	private Date creationDate;
	
	private Date expirationDate;
	
	private byte[] token;
	
	private String signingKeyId;
	
	private IGGAPIAuthenticator authenticator;
	
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
