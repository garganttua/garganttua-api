package com.garganttua.api.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import com.garganttua.api.spec.GGAPICrudAccess;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.ws.IGGAPIRestService;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@Slf4j
public class GGAPISecurityHelper implements IGGAPISecurityHelper {

	@Autowired
	private Optional<IGGAPIAuthenticationManager> authenticationManager;

	@Autowired
	private Optional<IGGAPIAuthorizationManager> authorizationManager;
	
	@Autowired
	private List<IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> engineServices;
	
//	@Autowired
//	@Qualifier(value = "dynamicServices")
//	private List<IGGAPIRestService<? extends IGGAPIEntity,? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> restServices;
	
	@Autowired
	private Optional<GGAPITenantVerifier> tenantVerifier;

	@Getter
	private ArrayList<IGGAPIAuthorization> authorizations;
	
	@Autowired
	private Optional<GGAPIRolesRestService> rolesRestService;
	
	@PostConstruct
	private void init(){
		this.authorizations = new ArrayList<IGGAPIAuthorization>();
		
		this.engineServices.forEach(service -> {
			List<IGGAPIAuthorization> serviceAuthorizations = service.createAuthorizations();
			this.authorizations.addAll(serviceAuthorizations);
		});
		
		if( this.rolesRestService.isPresent() ) {
			this.authorizations.addAll(this.rolesRestService.get().getCustomAuthorizations());
			this.rolesRestService.get().setRoles(this.authorizations);
		}
	}
	
	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) {
			
		this.authorizations.forEach(a -> {
			try {
				log.info("Applying security configuration {}", a);
				
				if( a.getAccess() == GGAPICrudAccess.authenticated || a.getAccess() == GGAPICrudAccess.owner ) {
					if( a.getAuthorization() != null && !a.getAuthorization().isEmpty() ) {
						http.authorizeHttpRequests().requestMatchers(a.getHttpMethod(), a.getEndpoint()).hasAnyAuthority(a.getAuthorization()).and().authorizeHttpRequests();
					} else {
						http.authorizeHttpRequests().requestMatchers(a.getHttpMethod(), a.getEndpoint()).authenticated().and().authorizeHttpRequests();
					}
					
					if( a.getAccess() == GGAPICrudAccess.owner && this.tenantVerifier.isPresent() ) {
						this.tenantVerifier.get().addOwnerRule(a.getAuthorization());
					}
					
				} else if( a.getAccess() == GGAPICrudAccess.anonymous){
					http.authorizeHttpRequests().requestMatchers(a.getHttpMethod(), a.getEndpoint()).permitAll().and().authorizeHttpRequests();
				}
				
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
