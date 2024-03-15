package com.garganttua.api.security.authentication;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
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
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.factory.GGAPIEntityIdentifier;
import com.garganttua.api.core.entity.factory.GGAPIFactoryException;
import com.garganttua.api.core.entity.factory.IGGAPIEntityFactory;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.engine.GGAPIDomain;
import com.garganttua.api.engine.registries.IGGAPIDomainsRegistry;
import com.garganttua.api.security.GGAPISecurityException;
import com.garganttua.api.security.authentication.dao.GGAPIDaoAuthenticationProvider;
import com.garganttua.api.security.authentication.dao.IGGAPIAuthenticationUserMapper;
import com.garganttua.api.security.authentication.entity.GGAPIEntityLoginPasswordAuthenticationProvider;
import com.garganttua.api.security.authentication.modes.loginpassword.GGAPIAuthenticatorLogin;
import com.garganttua.api.security.authentication.modes.loginpassword.GGAPIAuthenticatorPassword;

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
	private IGGAPIEntityFactory factory;

	private GGAPIDomain domain;

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
		
		this.domain = dDomainsRegistry.getAuthenticatorDomain();
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

	private IGGAPIAuthenticationProvider entityAuthenticator() throws GGAPISecurityException {					
		
		Class<?> authenticator = this.domain.entityClass;
				
		if( authenticator != null ) {
			String authoritiesField = GGAPIAuthenticationManager.checkAnnotationIsPresent(authenticator, GGAPIAuthenticatorAuthorities.class, GGAPIAuthenticationManager.getListStringType());
			String isAccountNonExpiredField = GGAPIAuthenticationManager.checkAnnotationIsPresent(authenticator, GGAPIAuthenticatorAccountNonExpired.class, boolean.class);
			String isAccountNonLockedField = GGAPIAuthenticationManager.checkAnnotationIsPresent(authenticator, GGAPIAuthenticatorAccountNonLocked.class, boolean.class);
			String isCredentialsNonExpiredField = GGAPIAuthenticationManager.checkAnnotationIsPresent(authenticator, GGAPIAuthenticatorCredentialsNonExpired.class, boolean.class);
			String isEnabledField = GGAPIAuthenticationManager.checkAnnotationIsPresent(authenticator, GGAPIAuthenticatorEnabled.class, boolean.class);
			switch(this.authenticationMode) {
				default:
				case loginpassword:
					
					String loginField = GGAPIAuthenticationManager.checkAnnotationIsPresent(authenticator, GGAPIAuthenticatorLogin.class, String.class);
					String passwordField = GGAPIAuthenticationManager.checkAnnotationIsPresent(authenticator, GGAPIAuthenticatorPassword.class,  String.class);
					
					provider = new GGAPIDaoAuthenticationProvider();
					((DaoAuthenticationProvider) provider).setUserDetailsService(new GGAPIEntityLoginPasswordAuthenticationProvider(this.domain, this.factory, this.magicTenantId, isAccountNonExpiredField, isAccountNonLockedField, isCredentialsNonExpiredField, isEnabledField, loginField, passwordField, authoritiesField));
					((DaoAuthenticationProvider) provider).setPasswordEncoder(this.getPasswordEncoder().get());
					break;
			}
		} else {
			throw new GGAPISecurityException("No class found with annotation @GGAPIAuthenticator");
		}
		return provider;
	}
	
	public static String checkAnnotationIsPresent(Class<?> authenticator, Class<? extends Annotation> annotation, Type fieldType) throws GGAPISecurityException {
        String fieldName = null;
        for (Field field : authenticator.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation)) {
                fieldName = field.getName();
                Type typeToCheck = field.getGenericType() ;
                
                if( fieldType instanceof ParameterizedType ) {
                	if( typeToCheck instanceof ParameterizedType ) {
                		ParameterizedType parameterizedType = (ParameterizedType) fieldType;
                		ParameterizedType parameterizedTypeToCheck = (ParameterizedType) typeToCheck;

                		Type rawType = parameterizedType.getRawType();
                		if (rawType.equals(field.getType()) ){
                			Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                			Type[] actualTypeArgumentsToCheck = parameterizedTypeToCheck.getActualTypeArguments();
                			
                			if( actualTypeArguments.length == actualTypeArgumentsToCheck.length) {
                				for( int i = 0; i < actualTypeArguments.length; i++ ) {
                					if( !actualTypeArguments[i].getTypeName().equals(actualTypeArgumentsToCheck[i].getTypeName()) ) {
                						throw new GGAPISecurityException("The field " + fieldName + " must be of type " + parameterizedType.getRawType()+ " with generics of type "+parameterizedType.getActualTypeArguments());
                					}
                				}
                			} else {
                    			throw new GGAPISecurityException("The field " + fieldName + " must be of type " + parameterizedType.getRawType()+ " with generics of type "+parameterizedType.getActualTypeArguments());
                    		}
                		} else {
                			throw new GGAPISecurityException("The field " + fieldName + " must be of type " + parameterizedType.getRawType()+ " with generics of type "+parameterizedType.getActualTypeArguments());
                		}
                	} else {
                		throw new GGAPISecurityException("The field " + fieldName + " must be of type " + fieldType.getTypeName());
                	}
                	
                } else {
	                if (!field.getType().equals(fieldType)) {
	                	throw new GGAPISecurityException("The field " + fieldName + " must be of type " + fieldType.getTypeName());
	                }
                }
            }
        }

        if (fieldName == null || fieldName.isEmpty()) {
            throw new GGAPISecurityException("Aucun champ annoté avec " + annotation.getName() + " trouvé");
        }

        return fieldName;
    }

 
    public static ParameterizedType getListStringType() {
        ParameterizedType parameterizedType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return parameterizedType;
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
		if( domain.authenticatorEntity ) {
			switch(this.authenticationType) {
			default:
			case dao:
				break;
			case entity:
				switch(this.authenticationMode) {
				default:
				case loginpassword:
					String passwordField = GGAPIAuthenticationManager.checkAnnotationIsPresent(entity.getClass(), GGAPIAuthenticatorPassword.class,  String.class);
					String password;
					try {
						password = GGAPIEntityHelper.getEntityFieldValue(entity, passwordField);
						String encodedPassword = this.getPasswordEncoder().get().encode(password);
						GGAPIEntityHelper.setEntityFieldValue(entity, passwordField, encodedPassword);
					} catch (SecurityException | IllegalArgumentException
							| GGAPIEntityException e) {
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
		Object entity = this.factory.getEntityFromRepository(this.domain, caller, null, GGAPIEntityIdentifier.UUID, entityUuid);
		authenticator = ((GGAPIDaoAuthenticationProvider) this.provider).getAuthenticatorFromEntity(tenantId, entity);
		return authenticator;
	}
}
