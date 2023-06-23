package com.garganttua.api.security.authentication;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.authentication.dao.ISpringCrudifyAuthenticationUserMapper;

@Service
@ConditionalOnProperty(name = "spring.domain.crudify.security.authentication", havingValue = "enabled", matchIfMissing = true)
public class SpringCrudifyAuthenticationManager implements ISpringCrudifyAuthenticationManager {

	@Inject
	private ISpringCrudifyAuthenticationUserMapper userMapper;
	
	@Value("${spring.domain.crudify.security.authentication.type}")
	private SpringCrudifyAuthenticationType authenticationType;

	@Value("${spring.domain.crudify.security.authentication.password.encoder}")
	private SpringCrudifyAuthenticationPasswordEncoder passwordEncoderType;
	
	@Bean
	private PasswordEncoder getPasswordEncoder() {
		
		PasswordEncoder encoder = null;
		switch (this.passwordEncoderType) {
		default:
		case bcrypt: 
			encoder = new BCryptPasswordEncoder();
			break;
		}

		return encoder;
	}
	
	public AuthenticationProvider authenticationProvider() throws ISpringCrudifySecurityException {
		AuthenticationProvider provider = null;
		
		switch(this.authenticationType) {
		default:
		case dao:
			provider = new DaoAuthenticationProvider();
			((DaoAuthenticationProvider) provider).setUserDetailsService(this.userMapper);
			((DaoAuthenticationProvider) provider).setPasswordEncoder(this.getPasswordEncoder());
			break;
		}
		
		return provider;
	}
	
	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws ISpringCrudifySecurityException {

		try {
			http.authorizeHttpRequests()
				.requestMatchers(HttpMethod.POST, "/authenticate").permitAll().and()
				.authenticationProvider(this.authenticationProvider())
				.authorizeHttpRequests().and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();
		} catch (Exception e) {
			new ISpringCrudifySecurityException(e);
		}
		
		return http;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	
}
