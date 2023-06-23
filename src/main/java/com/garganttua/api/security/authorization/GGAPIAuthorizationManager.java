package com.garganttua.api.security.authorization;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.authentication.IGGAPISecurityException;
import com.garganttua.api.security.authentication.dao.IGGAPIAuthenticationUserMapper;
import com.garganttua.api.security.authorization.bearer.GGAPIBearerAuthorizationExtractor;
import com.garganttua.api.security.authorization.token.jwt.GGAPIJwtTokenProvider;

@Service
@ConditionalOnProperty(name = "spring.domain.crudify.security.authentication", havingValue = "enabled", matchIfMissing = true)
public class GGAPIAuthorizationManager implements IGGAPIAuthorizationManager {

	@Value("${spring.domain.crudify.security.authorization}")
	private GGAPIAuthorizationType authorizationType;
	
	@Value("${spring.domain.crudify.security.authorization.token.type}")
	private GGAPITokenAuthorizationType tokenAuthorizationType;
	
	@Value("${spring.domain.crudify.security.authorization.token.provider}")
	private GGAPITokenProviderType tokenProviderType;
	
	@Inject
	private IGGAPIAuthenticationUserMapper userMapper;

	private GGAPIJwtTokenProvider authorizationProvider = null;

	@Value("${spring.domain.crudify.security.extractUserId}")
	private String extractUserId;
	
	@Bean 
	private IGGAPIAuthorizationProvider getAuthorizationProvider() {
		switch(this.authorizationType) {
		default:
		case token:
			switch (this.tokenAuthorizationType) {
			default:
			case jwt:
				switch (this.tokenProviderType) {
				case db:
					break;
				case inmemory: 
					break;
				default: 
				case none:
					this.authorizationProvider  = new GGAPIJwtTokenProvider();
					break;
				}
				break;
			}
			break;		
		}	
		return this.authorizationProvider;
	}
	
	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws IGGAPISecurityException {
		
		try {
			http.authorizeHttpRequests().and().addFilterBefore(new GGAPIBearerAuthorizationExtractor(this.authorizationProvider, this.userMapper, this.extractUserId), UsernamePasswordAuthenticationFilter.class);
		} catch (Exception e) {
			throw new IGGAPISecurityException(e);
		}
		
		return http;

	}

}
