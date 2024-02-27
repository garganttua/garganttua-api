package com.garganttua.api.security.authentication.ws;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.security.authentication.GGAPIAuthenticationMode;
import com.garganttua.api.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;
import com.garganttua.api.security.authentication.modes.loginpassword.GGAPILoginPasswordAuthenticationRequest;
import com.garganttua.api.security.authorization.BasicGGAPIAccessRule;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;
import com.garganttua.api.security.authorization.IGGAPIAuthorizationProvider;
import com.garganttua.api.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;
import com.garganttua.api.ws.GGAPIErrorObject;

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
			return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
		
        if (authentication.isAuthenticated()) {
        	
        	GGAPIToken authorization;
			try {
				authorization = this.authorizationProvider.getAuthorization(authentication);
				
			} catch (GGAPIKeyExpiredException | GGAPIEngineException | GGAPIEntityException e) {
				return new ResponseEntity<>("Error during authorization creation", HttpStatus.INTERNAL_SERVER_ERROR);
			}

        	return ResponseEntity.created(null)
                    .header(HttpHeaders.AUTHORIZATION,"Bearer "+new String(authorization.getToken()))
                    .header("Access-Control-Expose-Headers", "Authorization")
                    .body(((IGGAPIAuthenticator) authentication.getPrincipal()).getEntity());
        	
        } else {
        	return new ResponseEntity<>(new GGAPIErrorObject("Authentication failed"), HttpStatus.BAD_REQUEST);
          
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
	
	public List<IGGAPIAccessRule> getCustomAuthorizations() {
		List<IGGAPIAccessRule> auths = new ArrayList<IGGAPIAccessRule>();
		auths.add(new BasicGGAPIAccessRule("/authenticate", "authenticate", HttpMethod.POST, GGAPICrudAccess.anonymous));

		return auths;
	}

}
