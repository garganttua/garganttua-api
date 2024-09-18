package com.garganttua.api.interfaces.security.spring.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.stereotype.Service;

import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPIServiceMethodToHttpMethodBinder;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.security.spring.core.IGGAPISpringSecurityRestConfigurer;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringSecurityRestConfiguration {
	
	@Autowired
	private IGGAPIEngine engine;
	
	@Autowired
	private IGGAPISecurityEngine security;
	
	@Autowired
	private GGAPISpringOwnerVerifierFilter ownerVerifier;
	
	@Autowired
	private GGAPISpringTenantVerifierFilter tenantVerifier;
	
	@Autowired
	private GGAPISpringAuthorizationFilter authorizationFilter;
	
	@Value("${com.garganttua.api.spring.interface.rest.security.cors.enabled}")
	private boolean cors = false;
	
	@Value("${com.garganttua.api.spring.interface.rest.security.csrf.enabled}")
	private boolean csrf = false;
	
	@Autowired
	private GGAPICallerFilter callerFilter;
	
	@Autowired
	private List<IGGAPISpringSecurityRestConfigurer> configurers;
	
	@Autowired
	private GGAPISpringInterfaceRestSecurityApplicationFilter securityApplicationFilter;

	@Bean
	public DefaultSecurityFilterChain configureFilterChain(HttpSecurity http) throws GGAPISecurityException {
		
		try {
			if( !this.csrf ) {
				http.csrf().disable();
			}
			
			if( this.cors ) {
				http.cors();				
			}

			this.configurers.stream().forEach(config -> {
				try {
					config.configureFilterChain(http);
				} catch (Exception e) {
					log.atWarn().log("Error occured", e);
				}
			});
			
			this.configureSecurityFilterChainIfAuthorizationManagerIsPresent(http);

			this.security.ifAuthorizationManagerPresentOrElse((manager, caller) -> {
				try {
					http.authorizeHttpRequests().and().addFilterBefore(this.authorizationFilter, AuthorizationFilter.class);
					http.authorizeHttpRequests().and().addFilterBefore(this.callerFilter, GGAPISpringAuthorizationFilter.class);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} , null, () -> {
				try {
					http.authorizeHttpRequests().and().addFilterBefore(this.callerFilter, AuthorizationFilter.class);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

			
			this.security.ifTenantVerifierPresent((verifier, caller) -> {
				try {
					http.authorizeHttpRequests().and().addFilterAfter(this.tenantVerifier, AuthorizationFilter.class);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} , null);
			 
			this.security.ifOwnerVerifierPresent((verifier, caller) -> {
				try {
					http.authorizeHttpRequests().and().addFilterAfter(this.ownerVerifier, GGAPISpringTenantVerifierFilter.class);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} , null);
			
			http.authorizeHttpRequests().and().addFilterAfter(this.securityApplicationFilter, AuthorizationFilter.class);
			
			return http.build();
			
		} catch (Exception e) {
			throw new GGAPISecurityException(e);
		}
	}

	private void configureSecurityFilterChainIfAuthorizationManagerIsPresent(HttpSecurity http) throws GGAPIException {
		this.security.ifAuthorizationManagerPresentOrElse(
			(manager, caller) -> {
				try {
					this.configureAuthorizations(http);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}, 
			null, 
			() -> {
				try {
					http.authorizeHttpRequests().requestMatchers("**").permitAll();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		);
	}

	private void configureAuthorizations(HttpSecurity http) throws Exception {
		for( IGGAPIAccessRule accessRule: this.engine.getAccessRulesRegistry().getAccessRules() ) {

			log.info("Applying security configuration {}", accessRule);
			
			HttpMethod method = GGAPIServiceMethodToHttpMethodBinder.fromServiceMethod(accessRule.getOperation());
			
			if( accessRule.getAccess() == GGAPIServiceAccess.authenticated || accessRule.getAccess() == GGAPIServiceAccess.tenant || accessRule.getAccess() == GGAPIServiceAccess.owner ) {
				if( accessRule.getAuthority() != null && !accessRule.getAuthority().isEmpty() ) {
						http.authorizeHttpRequests().requestMatchers(method, accessRule.getEndpoint()).hasAnyAuthority(accessRule.getAuthority()).and().authorizeHttpRequests();
				} else {
					http.authorizeHttpRequests().requestMatchers(method, accessRule.getEndpoint()).authenticated().and().authorizeHttpRequests();
				}

			} else if( accessRule.getAccess() == GGAPIServiceAccess.anonymous){
				http.authorizeHttpRequests().requestMatchers(method, accessRule.getEndpoint()).permitAll().and().authorizeHttpRequests();
			}
		}
	}
}
