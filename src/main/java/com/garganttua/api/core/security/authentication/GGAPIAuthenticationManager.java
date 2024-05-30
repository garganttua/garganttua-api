package com.garganttua.api.core.security.authentication;

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

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIDomain;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.factory.GGAPIFactoryException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.objects.query.GGAPIObjectQuery;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryFactory;
import com.garganttua.api.core.objects.query.IGGAPIObjectQuery;
import com.garganttua.api.core.security.GGAPISecurityException;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorAccountNonExpired;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorAccountNonLocked;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorAuthorities;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorCredentialsNonExpired;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorEnabled;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorLogin;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorPassword;
import com.garganttua.api.core.security.authentication.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.core.security.authentication.entity.checker.GGAPIEntityAuthenticatorException;
import com.garganttua.api.core.security.authentication.entity.checker.GGAPIEntityAuthenticatorChecker.GGAPIAuthenticatorInfos;
import com.garganttua.api.core.security.authentication.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.authentication.mappers.GGAPIEntityLoginPasswordAuthenticationMapper;
import com.garganttua.api.core.security.authentication.mappers.IGGAPIAuthenticationUserMapper;
import com.garganttua.api.core.security.authentication.providers.dao.GGAPIDaoAuthenticationProvider;
import com.garganttua.api.core.security.authentication.tools.GGAPIAuthenticationPasswordEncoder;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthenticationProvider;
import com.garganttua.api.spec.security.IGGAPIAuthenticator;

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
	
	@Value("${com.garganttua.api.superTenantId}")
	protected String magicTenantId;
	
	private IGGAPIAuthenticationProvider provider;
	
	@Autowired
	private IGGAPIDomainsRegistry dDomainsRegistry;
	
	@Autowired
	private IGGAPIEntityFactory<Object> factory;

	private GGAPIDomain domain;

	private GGAPIAuthenticatorInfos infos;
	
	private IGGAPIObjectQuery query;

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
		this.domain = GGAPIEntityAuthenticatorHelper.getAuthenticatorDomain(this.dDomainsRegistry);
		try {
			this.infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(domain.entity.getValue0(), authenticationMode);
			this.query = GGAPIObjectQueryFactory.objectQuery(domain.entity.getValue0());
		} catch (GGAPIEntityAuthenticatorException | GGAPIObjectQueryException e) {
			throw new GGAPISecurityException(e);
		}
		
		this.provider = null;
		
		switch(this.authenticationType) {
		default:
		case dao:
			switch(this.authenticationMode) {
			default:
			case loginpassword:
				provider = new GGAPIDaoAuthenticationProvider();
				
				if( this.userMapper.isEmpty() ) {
					throw new GGAPISecurityException("No IGGAPIAuthenticationUserMapper provided");
				} else {
					((DaoAuthenticationProvider) this.provider).setUserDetailsService(this.userMapper.get());
					((DaoAuthenticationProvider) this.provider).setPasswordEncoder(this.getPasswordEncoder().get());
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

	private IGGAPIAuthenticationProvider entityAuthenticator() throws GGAPISecurityException {					
		
		Class<?> authenticator = this.domain.entity.getValue0();
				
		if( authenticator != null ) {
			switch(this.authenticationMode) {
				default:
				case loginpassword:
					provider = new GGAPIDaoAuthenticationProvider();
					((DaoAuthenticationProvider) this.provider).setUserDetailsService(new GGAPIEntityLoginPasswordAuthenticationMapper(this.domain, this.factory, this.magicTenantId, this.infos.isAccountNonExpiredFieldAddress(), this.infos.isAccountNonLockedFieldAddress(), this.infos.isCredentialsNonExpiredFieldAddress(), this.infos.isEnabledFieldAddress(), this.infos.loginFieldAddress(), this.infos.passwordFieldAddress(), this.infos.authoritiesFieldAddress()));
					((DaoAuthenticationProvider) this.provider).setPasswordEncoder(this.getPasswordEncoder().get());
					break;
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

	@Override
	public Object applySecurityOnAuthenticatorEntity(Object entity) throws GGAPISecurityException {
		if( this.domain.entity.getValue0().equals(entity.getClass()) ) {
			switch(this.authenticationType) {
			default:
			case dao:
				break;
			case entity:
				switch(this.authenticationMode) {
				default:
				case loginpassword:
					String password;
					try {
						password = (String) this.query.getValue(entity, this.infos.passwordFieldAddress());
						String encodedPassword = this.getPasswordEncoder().get().encode(password);
						this.query.setValue(entity, this.infos.passwordFieldAddress(), encodedPassword);
					} catch (SecurityException | IllegalArgumentException
							| GGAPIObjectQueryException e) {
						throw new GGAPISecurityException(e);
					}
					break;
				}
				break;
			}
		}
		return entity;
	}

	@Override
	public IGGAPIAuthenticator getAuthenticatorFromOwnerId(String tenantId, String ownerId) throws GGAPIEntityException, GGAPIFactoryException {
		IGGAPIAuthenticator authenticator = null;
		
		switch(this.authenticationType) {
		default:
		case dao:
			authenticator = getAuthenticatorFromEntity(tenantId, ownerId);
			break;
		case entity:
			authenticator = getAuthenticatorFromEntity(tenantId, ownerId);
			break;
		}
		return authenticator;
	}

	private IGGAPIAuthenticator getAuthenticatorFromEntity(String tenantId, String entityUuid) throws GGAPIEntityException, GGAPIFactoryException {
		IGGAPIAuthenticator authenticator;
		IGGAPICaller caller = new GGAPICaller();
		Object entity = this.factory.getEntityFromRepository(caller, null, GGAPIEntityIdentifier.UUID, entityUuid);
		authenticator = ((GGAPIDaoAuthenticationProvider) this.provider).getAuthenticatorFromEntity(tenantId, entity);
		return authenticator;
	}
}
