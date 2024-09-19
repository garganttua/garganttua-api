package com.garganttua.api.interfaces.security.spring.rest.swagger;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.spring.core.IGGAPISpringSecurityRestConfigurer;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import jakarta.annotation.PostConstruct;

@Service
public class GGAPISpringSecurityRestSwaggerConfigurer implements IGGAPISpringSecurityRestConfigurer {
	
	@Autowired
	private Optional<OpenAPI> openApi;
	
	@Autowired 
	private IGGAPISecurityEngine security;
	
	@Autowired
	private IGGAPIEngine engine;
	
	@PostConstruct
	private void init() throws GGAPIException {
		this.security.ifAuthorizationManagerPresent((manager, caller) -> {
			io.swagger.v3.oas.models.security.SecurityScheme scheme = new io.swagger.v3.oas.models.security.SecurityScheme();
			String authorizationType = manager.getAuthorizationType();
			String authorizationFormat = manager.getAuthorizationFormat();
			String authorizationProtocol = manager.getAuthorizationProtocol();
			Type type = null;
			if( authorizationType.equals("Token") ) {
				type = Type.HTTP;
			}
			if( authorizationFormat.equals("JWT") ) {
				scheme.bearerFormat("JWT");
			}
			if( authorizationProtocol.equals("Bearer") ) {
				scheme.scheme("bearer");				
			}
			
			scheme.type(type);
			this.openApi.get().schemaRequirement("Authorization", scheme);
			
		}, null);
		
		SecurityRequirement req = new SecurityRequirement().addList("Authorization");
		
		this.engine.getAccessRulesRegistry().getAccessRules().stream().forEach( accessRule -> {
			String endpoint = accessRule.getEndpoint().replace("*", "{uuid}");
			System.out.println(endpoint+" "+accessRule);
			PathItem path = this.openApi.get().getPaths().get(endpoint);
			Operation operation = null;
			switch(accessRule.getOperation()) {
			case create_one:
				operation = path.getPost();
				break;
			case delete_all:
			case delete_one:
				operation = path.getDelete();
				break;
			case read_all:
			case read_one:
				operation = path.getGet();
				break;
			case update_one:
				operation = path.getPatch();
				break;
			}
			if( operation != null && accessRule.getAccess() != GGAPIServiceAccess.anonymous)
				operation.addSecurityItem(req);
		});
	}
	
	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws Exception {
		if( this.openApi.isPresent() ) {
			http.authorizeHttpRequests().requestMatchers("/swagger-ui.html","/swagger-ui/**","/v3/**").permitAll();
		}
		return http;
	}

}
