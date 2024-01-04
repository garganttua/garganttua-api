package com.garganttua.api.security.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.GGAPISecurityException;
import com.garganttua.api.security.authorization.bearer.GGAPIBearerAuthorizationValidator;
import com.garganttua.api.security.authorization.token.jwt.GGAPIJwtDBTokenProvider;
import com.garganttua.api.security.authorization.token.jwt.GGAPIJwtTokenProvider;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security.authentication", havingValue = "enabled", matchIfMissing = true)
public class GGAPIAuthorizationManager implements IGGAPIAuthorizationManager {

	@Value("${com.garganttua.api.security.authorization}")
	private GGAPIAuthorizationType authorizationType;
	
	@Value("${com.garganttua.api.security.authorization.token.type}")
	private GGAPITokenAuthorizationType tokenAuthorizationType;
	
	@Value("${com.garganttua.api.security.authorization.token.provider}")
	private GGAPITokenProviderType tokenProviderType;
	
	private IGGAPIAuthorizationProvider authorizationProvider = null;

	@Bean(value = "AuthorizationProvider")
	private IGGAPIAuthorizationProvider getAuthorizationProvider() {
		switch(this.authorizationType) {
		default:
		case token:
			switch (this.tokenAuthorizationType) {
			default:
			case jwt:
				switch (this.tokenProviderType) {
				case db:
					this.authorizationProvider = new GGAPIJwtDBTokenProvider();
					break;
				case inmemory: 
					break;
				default: 
				case none:
					this.authorizationProvider = new GGAPIJwtTokenProvider();
					break;
				}
				break;
			}
			break;		
		}	
		return this.authorizationProvider;
	}
	
	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws GGAPISecurityException {	
		try {
			switch(this.authorizationType) {
			default:
			case token:
				http.authorizeHttpRequests().and().addFilterBefore(new GGAPIBearerAuthorizationValidator(this.authorizationProvider), UsernamePasswordAuthenticationFilter.class);
				break;
			}
		} catch (Exception e) {
			throw new GGAPISecurityException(e);
		}
		
		return http;
	}

}
