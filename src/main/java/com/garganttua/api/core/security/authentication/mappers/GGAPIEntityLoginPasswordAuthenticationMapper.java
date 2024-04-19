package com.garganttua.api.core.security.authentication.mappers;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIDomain;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.factory.GGAPIFactoryException;
import com.garganttua.api.core.entity.factory.IGGAPIEntityFactory;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.security.authentication.IGGAPIAuthenticator;
import com.garganttua.api.core.security.authentication.entity.GGAPIEntityAuthentication;
import com.garganttua.api.core.security.authentication.entity.GGAPIEntityAuthenticator;
import com.garganttua.api.core.security.authentication.entity.tools.GGAPIEntityAuthenticatorHelper;

public class GGAPIEntityLoginPasswordAuthenticationMapper implements IGGAPIAuthenticationUserMapper {

	private String superTenantId;
	private GGAPIObjectAddress loginField;
	private GGAPIObjectAddress passwordField;
	private GGAPIObjectAddress authoritiesField;
	private GGAPIObjectAddress isAccountNonExpiredField;
	private GGAPIObjectAddress isAccountNonLockedField;
	private GGAPIObjectAddress isCredentialsNonExpiredField;
	private GGAPIObjectAddress isEnabledField;
	private IGGAPIEntityFactory<Object> factory;
 
	public GGAPIEntityLoginPasswordAuthenticationMapper (
			GGAPIDomain domain, IGGAPIEntityFactory<Object> factory, String superTenantId,
			GGAPIObjectAddress isAccountNonExpiredField, GGAPIObjectAddress isAccountNonLockedField, GGAPIObjectAddress isCredentialsNonExpiredField,
			GGAPIObjectAddress isEnabledField, GGAPIObjectAddress loginField, GGAPIObjectAddress passwordField, GGAPIObjectAddress authoritiesField) {
		this.factory = factory;
		this.superTenantId = superTenantId;
		this.isAccountNonExpiredField = isAccountNonExpiredField;
		this.isAccountNonLockedField = isAccountNonLockedField;
		this.isCredentialsNonExpiredField = isCredentialsNonExpiredField;
		this.isEnabledField = isEnabledField;
		this.loginField = loginField;
		this.passwordField = passwordField;
		this.authoritiesField = authoritiesField;
	}

	@Override
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
		UserDetails userDetails = null;

		String fiterString = "{\"name\":\"$field\", \"value\":\""+this.loginField+"\",\"literals\":[{\"name\":\"$eq\",\"value\":\""+login+"\"}]}";
		
		ObjectMapper mapper = new ObjectMapper();
		GGAPILiteral filter = null;
		try {
			filter = mapper.readValue(fiterString, GGAPILiteral.class);
		} catch (JsonProcessingException e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
		try {
			GGAPICaller caller = new GGAPICaller();
			caller.setTenantId(this.superTenantId);
			caller.setSuperTenant(true);

			List<Object> entities = this.factory.getEntitiesFromRepository(caller, 0, 0, filter, null, null);
			
			if( entities.size() < 1 ) {
				throw new UsernameNotFoundException("Login "+login+" not found");
			}
			
			String tenantId = GGAPIEntityHelper.getTenantId(entities.get(0));
			
			userDetails = getAuthenticatorFromEntity(tenantId, entities.get(0));
			
		} catch (GGAPIEntityException | GGAPIFactoryException e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
		return userDetails;
	}

	@Override
	public IGGAPIAuthenticator getAuthenticatorFromEntity(String tenantId, Object entity) throws GGAPIEntityException {
		String uuid = GGAPIEntityHelper.getUuid(entity);
		
		String login = (String) GGAPIEntityAuthenticatorHelper.getLogin(this.loginField, entity);
		String password = (String) GGAPIEntityAuthenticatorHelper.getPassword(this.passwordField, entity);
		List<String> authorities = (List<String>) GGAPIEntityAuthenticatorHelper.getAuthorities(this.authoritiesField, entity);
		boolean isAccountNonExpired = (boolean) GGAPIEntityAuthenticatorHelper.isAccountNonExpired(this.isAccountNonExpiredField, entity);
		boolean isAccountNonLocked = (boolean) GGAPIEntityAuthenticatorHelper.isAccountNonLocked(this.isAccountNonLockedField, entity);
		boolean isCredentialsNonExpired = (boolean) GGAPIEntityAuthenticatorHelper.isCredentialsNonExpired(this.isCredentialsNonExpiredField, entity);
		boolean isEnabled = (boolean) GGAPIEntityAuthenticatorHelper.isEnabled(this.isEnabledField, entity);
		
		GGAPIEntityAuthenticator authenticator = new GGAPIEntityAuthenticator(password, authorities, login, isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired, isEnabled, uuid, tenantId, entity, null);
		new GGAPIEntityAuthentication(authenticator);
		return authenticator;
	}
}
