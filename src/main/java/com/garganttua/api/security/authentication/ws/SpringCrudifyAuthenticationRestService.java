package com.garganttua.api.security.authentication.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.garganttua.api.security.authentication.ISpringAuthenticationRequest;
import com.garganttua.api.security.authentication.SpringCrudifyAuthenticationMode;
import com.garganttua.api.security.authentication.modes.loginpassword.SpringCrudifyLoginPasswordAuthenticationRequest;
import com.garganttua.api.security.authorization.ISpringCrudifyAuthorizationProvider;
import com.garganttua.api.security.keys.SpringCrudifyKeyExpiredException;
import com.garganttua.api.ws.ISpringCrudifyErrorObject;

import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*")
@ComponentScan("com.citech.iot")
@Tag(name = "Auhtentication", description = "The Spring Domain Crudify built-in authentication API")
@RestController
@ConditionalOnProperty(name = "spring.domain.crudify.security.authentication", havingValue = "enabled", matchIfMissing = true)
public class SpringCrudifyAuthenticationRestService {
	
	@Value("${spring.domain.crudify.security.authentication.mode}")
	private SpringCrudifyAuthenticationMode authenticationMode;
	
	@Autowired
    private AuthenticationManager authenticationManager;

	@Autowired
	private ISpringCrudifyAuthorizationProvider authorizationProvider;
	
	@PostMapping("/authenticate")
	@ConditionalOnProperty(name = "spring.domain.crudify.security.authentication.mode", havingValue = "loginpassword", matchIfMissing = true)
    public ResponseEntity<?> authenticate(@RequestBody SpringCrudifyLoginPasswordAuthenticationRequest authenticationRequest) {
       
		Authentication authentication = this.getAuthentication(authenticationRequest);

		try {
			authentication = this.authenticationManager.authenticate(authentication);
		} catch (Exception e) {
			return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
		
        if (authentication.isAuthenticated()) {
        	
        	String authorization;
			try {
				authorization = this.authorizationProvider.getAuthorization(authentication);
			} catch (SpringCrudifyKeyExpiredException e) {
				return new ResponseEntity<>("Error during authorization creation", HttpStatus.INTERNAL_SERVER_ERROR);
			}
        	
        	return new ResponseEntity<>(authorization, HttpStatus.CREATED);
        	
        } else {
        	return new ResponseEntity<>(new ISpringCrudifyErrorObject("Authentication failed"), HttpStatus.BAD_REQUEST);
          
        }
    }

	private Authentication getAuthentication(ISpringAuthenticationRequest authenticationRequest) {
		UsernamePasswordAuthenticationToken authentication = null;
		switch(this.authenticationMode) {
		default:
		case loginpassword:

			String login = ((SpringCrudifyLoginPasswordAuthenticationRequest) authenticationRequest).getLogin();
			String password = ((SpringCrudifyLoginPasswordAuthenticationRequest) authenticationRequest).getPassword();
			
			authentication  = new UsernamePasswordAuthenticationToken(login, password);
			break;
		}
		return authentication;
	}

}
