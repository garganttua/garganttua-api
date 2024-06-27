package com.garganttua.api.core.security.authentication.ws;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.garganttua.api.core.domain.GGAPIDomain;
import com.garganttua.api.core.security.authorization.BasicGGAPIAccessRule;
import com.garganttua.api.spec.GGAPIServiceAccess;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPIServiceMethod;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;

@CrossOrigin(origins = "*")
@Tag(name = "Authorizations", description = "The Spring Domain Crudify built-in authorizations API")
@RestController
@ConditionalOnProperty(name = "com.garganttua.api.security.exposeAuthorities", havingValue = "enabled", matchIfMissing = true)
public class GGAPIAuthoritiesRestService implements IGGAPIEngineObject {
	
	@Setter
	private IGGAPIEngine engine;

	@GetMapping("/authorities")
	public ResponseEntity<?> getRoles() {
		List<String> auths = new ArrayList<String>();
		
		this.engine.getAccessRulesRegistry().getAccessRules().forEach(r -> {
			if( r.getAuthority() != null ) {
				auths.add(r.getAuthority());
			}
		});
		
		List<String> listWithoutDuplicates = new ArrayList<String>(new HashSet<>(auths));
        return new ResponseEntity<>(listWithoutDuplicates, HttpStatus.ACCEPTED);
    }

	public List<IGGAPIAccessRule> getCustomAuthorizations() {
		List<IGGAPIAccessRule> auths = new ArrayList<IGGAPIAccessRule>();
		auths.add(new BasicGGAPIAccessRule("/authorities", "authorities-read", GGAPIServiceMethod.READ, GGAPIServiceAccess.authenticated));
		return auths;
	}

	@Override
	public void setDomain(GGAPIDomain domain) {
	}
}
