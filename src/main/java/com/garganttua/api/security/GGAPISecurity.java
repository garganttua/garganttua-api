package com.garganttua.api.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.security.authentication.IGGAPIAuthenticationManager;
import com.garganttua.api.security.authentication.ws.GGAPIRolesRestService;
import com.garganttua.api.security.authorization.GGAPIAuthorityVerifier;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.security.authorization.IGGAPIAuthorizationManager;
import com.garganttua.api.security.tenants.GGAPIEngineTenantIdHeaderManager;
import com.garganttua.api.security.tenants.GGAPITenantVerifier;
import com.garganttua.api.spec.GGAPICrudAccess;

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
	private GGAPIEngineTenantIdHeaderManager tenantIdHeaderManager;

	@Autowired
	@Getter
	private Optional<IGGAPIAuthenticationManager> authenticationManager;

	@Autowired
	private Optional<IGGAPIAuthorizationManager> authorizationManager;
	
	@Autowired
	@Qualifier(value = "servicesRegistry")
	private IGGAPIServicesRegistry services;

	@Autowired
	private Optional<GGAPITenantVerifier> tenantVerifier;
	
	@Autowired
	private Optional<GGAPITenantVerifier> ownerVerifier;

	@Getter
	private ArrayList<IGGAPIAuthorization> authorizations;
	
	@Autowired
	private Optional<GGAPIRolesRestService> rolesRestService;
	
	@Autowired
	private Optional<GGAPIAuthorityVerifier> authorityVerifier;
	
	@Autowired
	private Optional<OpenAPI> openApi;
	
	@Value("${com.garganttua.api.security.cors.enabled}")
	private boolean cors = true;
	
	@Value("${com.garganttua.api.security.csrf.enabled}")
	private boolean csrf = true;
	
	@PostConstruct
	private void init(){
		this.authorizations = new ArrayList<IGGAPIAuthorization>();
		
		this.services.getServices().forEach(service -> {
			List<IGGAPIAuthorization> serviceAuthorizations = service.createAuthorizations();
			this.authorizations.addAll(serviceAuthorizations);
		});
		
		if( this.rolesRestService.isPresent() ) {
			this.authorizations.addAll(this.rolesRestService.get().getCustomAuthorizations());
			this.rolesRestService.get().setRoles(this.authorizations);
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
			
			for( IGGAPIAuthorization a: this.authorizations ) {

				log.info("Applying security configuration {}", a);
				
				if( a.getAccess() == GGAPICrudAccess.authenticated || a.getAccess() == GGAPICrudAccess.tenant || a.getAccess() == GGAPICrudAccess.owner ) {
					if( a.getAuthorization() != null && !a.getAuthorization().isEmpty() ) {
						http.authorizeHttpRequests().requestMatchers(a.getHttpMethod(), a.getEndpoint()).hasAnyAuthority(a.getAuthorization()).and().authorizeHttpRequests();
					} else {
						http.authorizeHttpRequests().requestMatchers(a.getHttpMethod(), a.getEndpoint()).authenticated().and().authorizeHttpRequests();
					}

					if( a.getAccess() == GGAPICrudAccess.tenant && this.tenantVerifier.isPresent() ) {
						this.tenantVerifier.get().addOwnerRule(a);
					}
					if( a.getAccess() == GGAPICrudAccess.owner && this.ownerVerifier.isPresent() ) {
						this.ownerVerifier.get().addOwnerRule(a);
					}
					this.authorityVerifier.get().addOwnerRuule(a);
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

			if( this.tenantVerifier.isPresent() ){
				http.authorizeHttpRequests().and().addFilterAfter(this.tenantVerifier.get(), UsernamePasswordAuthenticationFilter.class);
			}
		
			if( this.authorityVerifier.isPresent() ) {
				http.authorizeHttpRequests().and().addFilterAfter(this.authorityVerifier.get(), GGAPITenantVerifier.class);
			}
		
			if( this.ownerVerifier.isPresent() ) {
				http.authorizeHttpRequests().and().addFilterAfter(this.ownerVerifier.get(), GGAPITenantVerifier.class);
			}
		
			http.authorizeHttpRequests().and().addFilterBefore(this.tenantIdHeaderManager, UsernamePasswordAuthenticationFilter.class);

			return http.build();
		} catch (Exception e) {
			throw new GGAPISecurityException(e);
		}
	}
}
