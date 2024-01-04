package com.garganttua.api.security.authentication;

import java.lang.reflect.Field;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.engine.accessors.IGGAPIAuthenticatorAccessor;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.GGAPISecurityException;
import com.garganttua.api.security.authentication.dao.IGGAPIAuthenticationUserMapper;
import com.garganttua.api.security.authentication.entity.GGAPIEntityLoginPasswordAuthenticationProvider;
import com.garganttua.api.security.authentication.modes.loginpassword.GGAPIAuthenticatorLogin;
import com.garganttua.api.security.authentication.modes.loginpassword.IGGAPILoginPasswordAuthenticationEntity;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(name = "com.garganttua.api.security.authentication", havingValue = "enabled", matchIfMissing = true)
public class GGAPIAuthenticationManager implements IGGAPIAuthenticationManager {

	@Autowired
	protected Optional<IGGAPIAuthenticationUserMapper> userMapper;
	
	@Value("${com.garganttua.api.security.authentication.type}")
	private GGAPIAuthenticationType authenticationType;

	@Value("${com.garganttua.api.security.authentication.password.encoder}")
	private GGAPIAuthenticationPasswordEncoder passwordEncoderType;
	
	@Value("${com.garganttua.api.security.authentication.mode}")
	private GGAPIAuthenticationMode authenticationMode;
	
	@Value("${com.garganttua.api.magicTenantId}")
	protected String magicTenantId;
	
	@Autowired
	private IGGAPIAuthenticatorAccessor authenticatorAccessor;

	private AuthenticationProvider provider;

	public Optional<PasswordEncoder> getPasswordEncoder() {
		PasswordEncoder encoder = null;
		switch (this.passwordEncoderType) {
		default:
		case bcrypt: 
			encoder = new BCryptPasswordEncoder();
			break;
		}

		return Optional.ofNullable(encoder);
	}
	
	@Bean
	public AuthenticationProvider authenticationProvider() throws GGAPISecurityException {
		this.provider = null;
		
		switch(this.authenticationType) {
		default:
		case dao:
			switch(this.authenticationMode) {
			default:
			case loginpassword:
				provider = new DaoAuthenticationProvider();
				
				if( this.userMapper.isEmpty() ) {
					throw new GGAPISecurityException("No IGGAPIAuthenticationUserMapper provided");
				} else {
					((DaoAuthenticationProvider) provider).setUserDetailsService(this.userMapper.get());
					((DaoAuthenticationProvider) provider).setPasswordEncoder(this.getPasswordEncoder().get());
				}
				break;
			}
			break;
		case entity:
			provider = this.entityAuthenticator();
			break;
		}
		
		return provider;
	}

	private AuthenticationProvider entityAuthenticator() throws GGAPISecurityException {					
		IGGAPIController<IGGAPIAuthenticator, IGGAPIDTOObject<IGGAPIAuthenticator>> controller = this.authenticatorAccessor.getAuthenticatorController();	
		Class<IGGAPIAuthenticator> authenticator = this.authenticatorAccessor.getAuthenticator();
				
		if( authenticator != null ) {
			switch(this.authenticationMode) {
				default:
				case loginpassword:
					
					if (!IGGAPILoginPasswordAuthenticationEntity.class.isAssignableFrom(authenticator)) {
						throw new GGAPISecurityException(
								"The class [" + authenticator.getName() + "] must implements the IGGAPILoginPasswordAuthenticationEntity interface.");
					}
					String loginField = null;

					for( Field field: authenticator.getDeclaredFields() ) {
						if( field.isAnnotationPresent(GGAPIAuthenticatorLogin.class) ) {
							loginField = field.getName();
						}
					}
					
					if( loginField == null || loginField.isEmpty() ) {
						throw new GGAPISecurityException("No field annotated with @GGAPIAuthenticatorLogin found");
					}
					provider = new DaoAuthenticationProvider();
					((DaoAuthenticationProvider) provider).setUserDetailsService(new GGAPIEntityLoginPasswordAuthenticationProvider(controller, this.magicTenantId, loginField));
					((DaoAuthenticationProvider) provider).setPasswordEncoder(this.getPasswordEncoder().get());
			}
		} else {
			throw new GGAPISecurityException("No class found with annotation @GGAPIAuthenticator");
		}
		return provider;
	}

	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws GGAPISecurityException {

		try {
			http.authorizeHttpRequests()
				.requestMatchers(HttpMethod.POST, "/authenticate").permitAll().and()
				.authenticationProvider(this.provider)
				.authorizeHttpRequests().and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();
		} catch (Exception e) {
			throw new GGAPISecurityException(e);
		}
		
		return http;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
