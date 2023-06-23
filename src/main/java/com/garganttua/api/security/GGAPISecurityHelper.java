package com.garganttua.api.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authentication.IGGAPIAuthenticationManager;
import com.garganttua.api.security.authentication.IGGAPISecurityException;
import com.garganttua.api.security.authentication.ws.GGAPIRolesRestService;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.security.authorization.IGGAPIAuthorizationManager;
import com.garganttua.api.security.tenants.GGAPITenantVerifier;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.ws.AbstractGGAPIService;
import com.garganttua.api.ws.IGGAPIRestService;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@Slf4j
public class GGAPISecurityHelper implements IGGAPISecurityHelper {

	@Inject
	private Optional<IGGAPIAuthenticationManager> authenticationManager;

	@Inject
	private Optional<IGGAPIAuthorizationManager> authorizationManager;
	
	@Inject
	private List<AbstractGGAPIService<? extends IGGAPIEntity,? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> services;
	
	@Inject
	private List<IGGAPIRestService<? extends IGGAPIEntity,? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> restServices;
	
	@Inject
	private Optional<GGAPITenantVerifier> tenantVerifier;

	@Getter
	private ArrayList<IGGAPIAuthorization> authorizations;
	
	@Inject
	private Optional<GGAPIRolesRestService> rolesRestService;
	
	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws IGGAPISecurityException {
		
		this.authorizations = new ArrayList<IGGAPIAuthorization>();
		
		this.services.forEach(service -> {
			List<IGGAPIAuthorization> serviceAuthorizations = service.createAuthorizations();
			this.authorizations.addAll(serviceAuthorizations);
		});
		
		this.restServices.forEach(service -> {
			List<IGGAPIAuthorization> serviceAuthorizations = service.createAuthorizations();
			this.authorizations.addAll(serviceAuthorizations);
		});
		
		if( this.rolesRestService.isPresent() ) {
			this.authorizations.addAll(this.rolesRestService.get().getCustomAuthorizations());
			
			this.rolesRestService.get().setRoles(this.authorizations);
		}
		
		this.authorizations.forEach(a -> {
			try {
				log.info("Created Basic Authorization {}", a);
				http.authorizeHttpRequests().requestMatchers(a.getHttpMethod(), a.getEndpoint()).hasAnyAuthority(a.getRole());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		this.authenticationManager.ifPresent(a -> {
			try {
				a.configureFilterChain(http);
			} catch (IGGAPISecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		this.authorizationManager.ifPresent(a -> {
			try {
				a.configureFilterChain(http);
			} catch (IGGAPISecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		this.tenantVerifier.ifPresent(t -> {
			try {
				http.authorizeHttpRequests().and().addFilterAfter(t, UsernamePasswordAuthenticationFilter.class);
			} catch (Exception e) {
				
			}
		});

		return http;

	}

}
