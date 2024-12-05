package com.garganttua.api.security.authentication.interfaces.spring.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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

	public GGAPISpringRestAuthenticationResponse(Object authentication) throws GGAPIException {
		this.domain = GGAPIAuthenticationHelper.getAuthenticatorService(authentication).getDomain().getDomain();
		this.principal = GGAPIAuthenticationHelper.getPrincipal(authentication);
		Object authorization = GGAPIAuthenticationHelper.getAuthorization(authentication);
		if( authorization != null ) {
			this.authorization = new String(GGAPIEntityAuthorizationHelper.toByteArray(authorization));
			this.authorizationType = GGAPIEntityAuthorizationHelper.getType(authorization);
		}
	}
}
