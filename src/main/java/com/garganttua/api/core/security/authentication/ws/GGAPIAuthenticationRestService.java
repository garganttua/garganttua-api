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
import com.garganttua.api.core.security.authentication.ws.requests.GGAPILoginPasswordAuthenticationRequest;
import com.garganttua.api.core.security.authorization.GGAPIAuthorizationProviderException;
import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.core.service.GGAPIErrorObject;
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
	
	@PostMapping("/authenticate")
	@ConditionalOnProperty(name = "com.garganttua.api.security.authentication.mode", havingValue = "loginpassword", matchIfMissing = true)
    public ResponseEntity<?> authenticate(@RequestBody GGAPILoginPasswordAuthenticationRequest authenticationRequest) {
       
		Authentication authentication = this.getAuthentication(authenticationRequest);
		
		try {
			authentication = this.authenticationManager.authenticate(authentication);
		} catch (Exception e) {
			return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage(), GGAPICoreExceptionCode.FAILED_AUTHENTICATION.getCode()), HttpStatus.BAD_REQUEST);
		}
		
        if (authentication.isAuthenticated()) {
        	
        	GGAPIToken authorization;
			try {
				authorization = this.authorizationProvider.getAuthorization(authentication);
				
			} catch (GGAPIAuthorizationProviderException e) {
				return new ResponseEntity<>("Error during authorization creation", HttpStatus.INTERNAL_SERVER_ERROR);
			}

        	return ResponseEntity.created(null)
                    .header(HttpHeaders.AUTHORIZATION,"Bearer "+new String(authorization.getToken()))
                    .header("Access-Control-Expose-Headers", "Authorization")
                    .body(((IGGAPIAuthenticator) authentication.getPrincipal()).getEntity());
        	
        } else {
        	return new ResponseEntity<>(new GGAPIErrorObject("Authentication failed", GGAPICoreExceptionCode.FAILED_AUTHENTICATION.getCode()), HttpStatus.BAD_REQUEST);
          
        }
    }

	private Authentication getAuthentication(IGGAPIAuthenticationRequest authenticationRequest) {
		Authentication authentication = null;
		switch(this.authenticationMode) {
		default:
		case loginpassword:

			String login = ((GGAPILoginPasswordAuthenticationRequest) authenticationRequest).getLogin();
			String password = ((GGAPILoginPasswordAuthenticationRequest) authenticationRequest).getPassword();
			
			authentication  = new UsernamePasswordAuthenticationToken(login, password);
			break;
		}
		return authentication;
	}

}
