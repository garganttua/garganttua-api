package com.garganttua.api.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.engine.registries.IGGAPIDynamicDomainsRegistry;
import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authentication.IGGAPIAuthenticationManager;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;
import com.garganttua.api.security.authorization.IGGAPIAuthorizationManager;
import com.garganttua.api.security.owners.GGAPIOwnerVerifier;
import com.garganttua.api.security.tenants.GGAPITenantVerifier;
import com.garganttua.api.ws.IGGAPIRestService;
import com.garganttua.api.ws.filters.GGAPIDynamicDomainFilter;
import com.garganttua.api.ws.filters.GGAPIOwnerFilter;
import com.garganttua.api.ws.filters.GGAPITenantFilter;

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
public class GGAPISecurity implements IGGAPISecurity {
	
	@Autowired
	@Getter
	private Optional<IGGAPIAuthenticationManager> authenticationManager;

	@Autowired
	private Optional<IGGAPIAuthorizationManager> authorizationManager;
	
	@Autowired
	private IGGAPIAccessRulesRegistry accessRulesRegistry;
	
	@Autowired
	private IGGAPIDynamicDomainsRegistry dDomainsRegistry;
	
	@Autowired
	private IGGAPIServicesRegistry servicesRegistry;

	@Getter
	@Autowired
	private Optional<GGAPITenantVerifier> tenantVerifier;
	
	@Getter
	@Autowired
	private Optional<GGAPIOwnerVerifier> ownerVerifier;
		
	@Autowired 
	private GGAPIDynamicDomainFilter ddomainFilter;
	
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
		GGAPIDynamicDomain domain = this.dDomainsRegistry.getAuthenticatorDomain();
		if( domain != null ) {
			IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> s = this.servicesRegistry.getService(domain.domain);
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
				
				if( a.getAccess() == GGAPICrudAccess.authenticated || a.getAccess() == GGAPICrudAccess.tenant || a.getAccess() == GGAPICrudAccess.owner ) {
					if( a.getAuthority() != null && !a.getAuthority().isEmpty() ) {
						http.authorizeHttpRequests().requestMatchers(a.getHttpMethod(), a.getEndpoint()).hasAnyAuthority(a.getAuthority()).and().authorizeHttpRequests();
					} else {
						http.authorizeHttpRequests().requestMatchers(a.getHttpMethod(), a.getEndpoint()).authenticated().and().authorizeHttpRequests();
					}

				} else if( a.getAccess() == GGAPICrudAccess.anonymous){
					http.authorizeHttpRequests().requestMatchers(a.getHttpMethod(), a.getEndpoint()).permitAll().and().authorizeHttpRequests();
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
