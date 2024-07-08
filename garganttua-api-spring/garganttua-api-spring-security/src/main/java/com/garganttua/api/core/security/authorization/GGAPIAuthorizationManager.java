package com.garganttua.api.core.security.authorization;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.security.GGAPISecurityException;
import com.garganttua.api.core.security.authorization.bearer.GGAPIBearerAuthorizationValidator;
import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.core.security.authorization.tokens.jwt.GGAPIJwtDBTokenProvider;
import com.garganttua.api.core.security.authorization.tokens.jwt.GGAPIJwtTokenProvider;
import com.garganttua.api.spec.engine.IGGAPIRepositoriesRegistry;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorizationProvider;
import com.garganttua.api.spec.security.IGGAPIDBTokenKeeper;

import lombok.Getter;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security.authentication", havingValue = "enabled", matchIfMissing = true)
public class GGAPIAuthorizationManager implements IGGAPIAuthorizationManager {

	@Value("${com.garganttua.api.security.authorization}")
	private GGAPIAuthorizationType authorizationType;
	
	@Value("${com.garganttua.api.security.authorization.tokens.type}")
	private GGAPITokenAuthorizationType tokenAuthorizationType;
	
	@Value("${com.garganttua.api.security.authorization.tokens.provider}")
	private GGAPITokenProviderType tokenProviderType;
	
	@Getter
	private IGGAPIAuthorizationProvider authorizationProvider = null;

	@Autowired
	private IGGAPIAuthenticationManager authenticationManager;
	
	@Autowired 
	private Optional<IGGAPIDBTokenKeeper> tokenKeeper;
	
	@Autowired
	private IGGAPIRepositoriesRegistry repositoriesRegistry;
	
	@Bean(value = "AuthorizationProvider")
	private IGGAPIAuthorizationProvider getAuthorizationProviderForStartup() {
		switch(this.authorizationType) {
		default:
		case token:
			switch (this.tokenAuthorizationType) {
			default:
			case jwt:
				switch (this.tokenProviderType) {
				case db:
					this.authorizationProvider = new GGAPIJwtDBTokenProvider();
					((GGAPIJwtDBTokenProvider) this.authorizationProvider).setTokenKeeper(this.tokenKeeper);
					break;
				case inmemory: 
					break;
				case mongo: 
					this.authorizationProvider = new GGAPIJwtDBTokenProvider();
					IGGAPIDBTokenKeeper repository = (IGGAPIDBTokenKeeper) this.repositoriesRegistry.getRepository(GGAPIToken.domain);
					((GGAPIJwtDBTokenProvider) this.authorizationProvider).setTokenKeeper(Optional.ofNullable(repository));
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
				http.authorizeHttpRequests().and().addFilterBefore(new GGAPIBearerAuthorizationValidator(this.authorizationProvider, this.authenticationManager), UsernamePasswordAuthenticationFilter.class);
				break;
			}
		} catch (Exception e) {
			throw new GGAPISecurityException(e);
		}
		
		return http;
	}

}
