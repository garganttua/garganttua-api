package com.garganttua.api.core.security.authentication.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.garganttua.api.core.security.authentication.GGAPIAuthenticationMode;
import com.garganttua.api.core.security.authorization.GGAPIAuthorizationProviderException;
import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.core.service.GGAPIErrorObject;
import com.garganttua.api.security.spring.authentication.loginpassword.rest.GGAPILoginPasswordAuthenticationRequest;
import com.garganttua.api.spec.GGAPICoreExceptionCode;
import com.garganttua.api.spec.security.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.security.IGGAPIAuthenticator;
import com.garganttua.api.spec.security.IGGAPIAuthorizationProvider;

import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "The Garganttua API built-in authentication API")
@RestController
@ConditionalOnProperty(name = "com.garganttua.api.security.authentication", havingValue = "enabled", matchIfMissing = true)
public class GGAPIAuthenticationRestService {
		
	@Value("${com.garganttua.api.security.authentication.mode}")
	private GGAPIAuthenticationMode authenticationMode;
	
	@Autowired
    private AuthenticationManager authenticationManager;

	@Autowired
	private IGGAPIAuthorizationProvider authorizationProvider;
	
	

}
