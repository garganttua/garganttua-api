package com.garganttua.api.security.authorization.tokens.storage;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.core.dto.annotations.GGAPIDtoTenantId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "access-tokens")
public class GGAPITokenDTO {
	
	@Field
	@GGAPIDtoTenantId
	private String tenantId; 
		
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
	
	
}
