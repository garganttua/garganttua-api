package com.garganttua.api.security.authentication.interfaces.spring.rest;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.security.authentication.AuthenticationHelper;
import com.garganttua.api.core.security.authorization.EntityAuthorizationHelper;
import com.garganttua.api.spec.CoreException;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SpringRestAuthenticationResponse {

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

	public SpringRestAuthenticationResponse(Object authentication) throws CoreException {
		this.domain = AuthenticationHelper.getAuthenticatorService(authentication).getDomain().getDomain();
		this.principal = AuthenticationHelper.getPrincipal(authentication);
		Object authorization = AuthenticationHelper.getAuthorization(authentication);
		if (authorization != null) {
			this.authorization = new String(EntityAuthorizationHelper.toByteArray(authorization));
			this.authorizationType = EntityAuthorizationHelper.getType(authorization);
			if (EntityAuthorizationHelper.isRenewable(authorization.getClass())) {
				this.refreshToken = new String(
						Base64.getEncoder().encodeToString(EntityAuthorizationHelper.getRefreshToken(authorization)));
				this.refreshTokenExpirationDate = EntityAuthorizationHelper
						.getRefreshTokenExpirationDate(authorization);
			}
		}
	}

	public <T> T getPrincipalAs(Class<T> valueType) {
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
