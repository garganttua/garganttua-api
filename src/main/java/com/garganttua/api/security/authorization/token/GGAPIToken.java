package com.garganttua.api.security.authorization.token;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
	
public class GGAPIToken {
	
	private String uuid;
	
	private String userId;
	
	private Date creationDate;
	
	private Date expirationDate;
	
	private byte[] token;
	
	private String signingKeyId;
	
	@Override
	public int hashCode() {	
		return this.creationDate.hashCode() *
				this.expirationDate.hashCode() *
				this.uuid.hashCode() *
				this.userId.hashCode() *
				this.token.hashCode() *
				this.signingKeyId.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		GGAPIToken token = (GGAPIToken) obj;
		return this.creationDate.equals(token.creationDate) 
				&& this.expirationDate.equals(token.expirationDate)
				&& this.uuid.equals(token.getUuid())
				&& this.userId.equals(token.uuid)
				&& this.token.equals(token.token)
				&& this.signingKeyId.equals(token.signingKeyId);
	}
	
	@Override
	public String toString() {
		return this.uuid+":"+this.userId+":"+this.signingKeyId+":"+this.creationDate+":"+this.expirationDate+":"+this.token;
	}

}
