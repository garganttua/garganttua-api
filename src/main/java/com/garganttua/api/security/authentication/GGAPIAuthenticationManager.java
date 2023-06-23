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

import com.garganttua.api.security.authentication.dao.IGGAPIAuthenticationUserMapper;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security.authentication", havingValue = "enabled", matchIfMissing = true)
public class GGAPIAuthenticationManager implements IGGAPIAuthenticationManager {

	@Inject
	private IGGAPIAuthenticationUserMapper userMapper;
	
	@Value("${com.garganttua.api.security.authentication.type}")
	private GGAPIAuthenticationType authenticationType;

	@Value("${com.garganttua.api.security.authentication.password.encoder}")
	private GGAPIAuthenticationPasswordEncoder passwordEncoderType;
	
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
	
	public AuthenticationProvider authenticationProvider() throws IGGAPISecurityException {
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
	public HttpSecurity configureFilterChain(HttpSecurity http) throws IGGAPISecurityException {

		try {
			http.authorizeHttpRequests()
				.requestMatchers(HttpMethod.POST, "/authenticate").permitAll().and()
				.authenticationProvider(this.authenticationProvider())
				.authorizeHttpRequests().and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();
		} catch (Exception e) {
			new IGGAPISecurityException(e);
		}
		
		return http;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	
}
