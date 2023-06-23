package com.garganttua.api.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.security.authentication.ISpringCrudifyAuthenticationManager;
import com.garganttua.api.security.authentication.ISpringCrudifySecurityException;
import com.garganttua.api.security.authentication.ws.RolesRestService;
import com.garganttua.api.security.authorization.ISpringCrudifyAuthorization;
import com.garganttua.api.security.authorization.ISpringCrudifyAuthorizationManager;
import com.garganttua.api.security.tenants.SpringCrudifyTenantVerifier;
import com.garganttua.api.spec.ISpringCrudifyEntity;
import com.garganttua.api.ws.AbstractSpringCrudifyService;
import com.garganttua.api.ws.ISpringCrudifyRestService;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(name = "spring.domain.crudify.security", havingValue = "enabled", matchIfMissing = true)
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@Slf4j
public class SpringCrudifySecurityHelper implements ISpringCrudifySecurityHelper {

	@Inject
	private Optional<ISpringCrudifyAuthenticationManager> authenticationManager;

	@Inject
	private Optional<ISpringCrudifyAuthorizationManager> authorizationManager;
	
	@Inject
	private List<AbstractSpringCrudifyService<? extends ISpringCrudifyEntity,? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>> services;
	
	@Inject
	private List<ISpringCrudifyRestService<? extends ISpringCrudifyEntity,? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>>> restServices;
	
	@Inject
	private Optional<SpringCrudifyTenantVerifier> tenantVerifier;

	@Getter
	private ArrayList<ISpringCrudifyAuthorization> authorizations;
	
	@Inject
	private Optional<RolesRestService> rolesRestService;
	
	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws ISpringCrudifySecurityException {
		
		this.authorizations = new ArrayList<ISpringCrudifyAuthorization>();
		
		this.services.forEach(service -> {
			List<ISpringCrudifyAuthorization> serviceAuthorizations = service.createAuthorizations();
			this.authorizations.addAll(serviceAuthorizations);
		});
		
		this.restServices.forEach(service -> {
			List<ISpringCrudifyAuthorization> serviceAuthorizations = service.createAuthorizations();
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
			} catch (ISpringCrudifySecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		this.authorizationManager.ifPresent(a -> {
			try {
				a.configureFilterChain(http);
			} catch (ISpringCrudifySecurityException e) {
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
