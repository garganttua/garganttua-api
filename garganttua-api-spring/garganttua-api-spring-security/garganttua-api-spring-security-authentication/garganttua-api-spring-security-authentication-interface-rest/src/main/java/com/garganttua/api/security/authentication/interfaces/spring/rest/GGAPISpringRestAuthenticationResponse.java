package com.garganttua.api.security.authentication.interfaces.spring.rest;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationHelper;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.spec.GGAPIException;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class GGAPISpringRestAuthenticationResponse {

	@JsonProperty
	private Object principal;
	@JsonProperty
	private String domain;
	@JsonProperty
	@JsonFormat(shape = JsonFormat.Shape.ARRAY)
	private String authorization;
	@JsonProperty
	private String authorizationType;
	@JsonProperty
	@JsonFormat(shape = JsonFormat.Shape.ARRAY)
	private String refreshToken;
	@JsonProperty
	private Date refreshTokenExpirationDate;

	public GGAPISpringRestAuthenticationResponse(Object authentication) throws GGAPIException {
		this.domain = GGAPIAuthenticationHelper.getAuthenticatorService(authentication).getDomain().getDomain();
		this.principal = GGAPIAuthenticationHelper.getPrincipal(authentication);
		Object authorization = GGAPIAuthenticationHelper.getAuthorization(authentication);
		if( authorization != null ) {
			this.authorization = new String(GGAPIEntityAuthorizationHelper.toByteArray(authorization));
			this.authorizationType = GGAPIEntityAuthorizationHelper.getType(authorization);
			if( GGAPIEntityAuthorizationHelper.isRenewable(authorization.getClass()) ) {
				this.refreshToken = new String(Base64.getEncoder().encodeToString(GGAPIEntityAuthorizationHelper.getRefreshToken(authorization)));
				this.refreshTokenExpirationDate = GGAPIEntityAuthorizationHelper.getRefreshTokenExpirationDate(authorization);
			}
		}
	}
	
	public <T> T getPrincipalAs(Class<T> valueType){
		ObjectMapper mapper = new ObjectMapper();
		
		byte[] principalAsBytes;
		try {
			principalAsBytes = mapper.writeValueAsBytes(this.principal);
			T value = mapper.readValue(principalAsBytes, valueType);
			return value;		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
