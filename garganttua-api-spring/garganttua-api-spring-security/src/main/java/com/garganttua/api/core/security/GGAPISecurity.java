package com.garganttua.api.core.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.domain.GGAPIDomain;
import com.garganttua.api.core.security.owners.GGAPIOwnerVerifier;
import com.garganttua.api.core.security.tenants.GGAPITenantVerifier;
import com.garganttua.api.interfaces.spring.rest.GGAPIServiceMethodToHttpMethodBinder;
import com.garganttua.api.interfaces.spring.rest.GGAPIDomainFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPIOwnerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPITenantFilter;
import com.garganttua.api.security.core.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.spec.GGAPIServiceAccess;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.service.IGGAPIServicesRegistryyport com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;
import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@Slf4j
@EnableWebSecurity
public class GGAPISecurity implements IGGAPISecurityEngine {
	
	@Autowired
	@Getter
	private Optional<IGGAPIAuthenticationManager> authenticationManager;

	@Autowired
	private Optional<IGGAPIAuthorizationManager> authorizationManager;
	
	@Autowired
	private IGGAPIAccessRulesRegistry accessRulesRegistry;
	
	@Autowired
	private IGGAPIDomainsRegistry dDomainsRegistry;
	
	@Autowired
	private IGGAPIServicesRegistry servicesRegistry;

	@Getter
	@Autowired
	private Optional<GGAPITenantVerifier> tenantVerifier;
	
	@Getter
	@Autowired
	private Optional<GGAPIOwnerVerifier> ownerVerifier;
		
	@Autowired 
	private GGAPIDomainFilter ddomainFilter;
	
	@Autowired
	private GGAPITenantFilter tenantFilter;
	
	@Autowired
	private GGAPIOwnerFilter ownerFilter;
	
	@Autowired
	private Optional<OpenAPI> openApi;
	
	@Value("${com.garganttua.api.security.cors.enabled}")
	private boolean cors = false;
	
	@Value("${com.garganttua.api.security.csrf.enabled}")
	private boolean csrf = false;
	
	@PostConstruct
	private void init() {
		GGAPIDomain domain = GGAPIEntityAuthenticatorHelper.getAuthenticatorDomain(this.dDomainsRegistry);
		if( domain != null ) {
			IGGAPIService s = this.servicesRegistry.getService(domain.entity.getValue1().domain());
			if( s != null ) {
				s.setSecurity(Optional.of(this));
			}
		}
	}

	
	@Override
	@Bean
	public DefaultSecurityFilterChain configureFilterChain(HttpSecurity http) throws GGAPISecurityException {
		
		try {
			if( !this.csrf ) {
				http.csrf().disable();
			}
			
			if( this.cors ) {
				http.cors();				
			}
			
			if( this.openApi.isPresent() ) {
				http.authorizeHttpRequests().requestMatchers("/swagger-ui.html","/swagger-ui/**","/v3/**").permitAll().and().authorizeHttpRequests();
			}
			
			for( IGGAPIAccessRule a: this.accessRulesRegistry.getAccessRules() ) {

				log.info("Applying security configuration {}", a);
				
				HttpMethod method = GGAPIServiceMethodToHttpMethodBinder.fromServiceMethod(a.getMethod());
				
				if( a.getAccess() == GGAPIServiceAccess.authenticated || a.getAccess() == GGAPIServiceAccess.tenant || a.getAccess() == GGAPIServiceAccess.owner ) {
					if( a.getAuthority() != null && !a.getAuthority().isEmpty() ) {
						http.authorizeHttpRequests().requestMatchers(method, a.getEndpoint()).hasAnyAuthority(a.getAuthority()).and().authorizeHttpRequests();
					} else {
						http.authorizeHttpRequests().requestMatchers(method, a.getEndpoint()).authenticated().and().authorizeHttpRequests();
					}

				} else if( a.getAccess() == GGAPIServiceAccess.anonymous){
					http.authorizeHttpRequests().requestMatchers(method, a.getEndpoint()).permitAll().and().authorizeHttpRequests();
				}
			}

			if( this.authenticationManager.isPresent() ) {
				this.authenticationManager.get().configureFilterChain(http);
			}
			
			if( this.authorizationManager.isPresent() ){
				this.authorizationManager.get().configureFilterChain(http);
			}

			http.authorizeHttpRequests().and().addFilterAfter(this.tenantFilter, AuthorizationFilter.class);
			http.authorizeHttpRequests().and().addFilterAfter(this.ownerFilter, AuthorizationFilter.class);
			http.authorizeHttpRequests().and().addFilterAfter(this.ddomainFilter, AuthorizationFilter.class);
			
			if( this.tenantVerifier.isPresent() ){
				http.authorizeHttpRequests().and().addFilterAfter(this.tenantVerifier.get(), AuthorizationFilter.class);
			}

			if( this.ownerVerifier.isPresent() ) {
				http.authorizeHttpRequests().and().addFilterAfter(this.ownerVerifier.get(), AuthorizationFilter.class);
			}

			return http.build();
		} catch (Exception e) {
			throw new GGAPISecurityException(e);
		}
	}

	@Override
	public Optional<IGGAPIAuthorizationManager> getAuthorizationManager() {
		return this.authorizationManager;
	}
}
