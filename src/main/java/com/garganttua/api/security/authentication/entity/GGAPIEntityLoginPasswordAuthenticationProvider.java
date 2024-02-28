package com.garganttua.api.security.authentication.entity;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.factory.IGGAPIEntityFactory;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityWithTenant;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;
import com.garganttua.api.security.authentication.dao.IGGAPIAuthenticationUserMapper;

public class GGAPIEntityLoginPasswordAuthenticationProvider implements IGGAPIAuthenticationUserMapper {

	private String superTenantId;
	private String loginField;
	private String passwordField;
	private String authoritiesField;
	private String isAccountNonExpiredField;
	private String isAccountNonLockedField;
	private String isCredentialsNonExpiredField;
	private String isEnabledField;
	private IGGAPIEntityFactory factory;
	private GGAPIDynamicDomain domain;
 
	public GGAPIEntityLoginPasswordAuthenticationProvider (
			GGAPIDynamicDomain domain, IGGAPIEntityFactory factory, String superTenantId,
			String isAccountNonExpiredField, String isAccountNonLockedField, String isCredentialsNonExpiredField,
			String isEnabledField, String loginField, String passwordField, String authoritiesField) {
		this.domain = domain;
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

	@SuppressWarnings("unchecked")
	@Override
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
		UserDetails userDetails = null;

		String fiterString = "{\"name\":\"$field\", \"value\":\""+this.loginField+"\",\"literals\":[{\"name\":\"$eq\",\"value\":\""+login+"\"}]}";
		
		ObjectMapper mapper = new ObjectMapper();
		GGAPILiteral filter = null;
		try {
			filter = mapper.readValue(fiterString, GGAPILiteral.class);
		} catch (JsonProcessingException e) {
			
		}
		try {
			GGAPICaller caller = new GGAPICaller();
			caller.setTenantId(this.superTenantId);
			caller.setSuperTenant(true);
			
			
			List<? extends IGGAPIEntity> entities = this.factory.getEntitiesFromRepository(this.domain, caller, 0, 0, filter, null, null, null);
			
			if( entities.size() < 1 ) {
				throw new UsernameNotFoundException("Login "+login+" not found");
			}
			
			userDetails = getAuthenticatorFromEntity(((IGGAPIEntityWithTenant) entities.get(0)).getTenantId(), entities.get(0).getClass(), entities.get(0));
			
		} catch (GGAPIEntityException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
		return userDetails;
	}

	@SuppressWarnings("unchecked")
	private UserDetails getAuthenticatorFromEntity(String tenantId, Class<?> entityClass, IGGAPIEntity entity) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		String login = (String) GGAPIEntityHelper.getFieldValue(entityClass, this.loginField, entity);
		String password = (String) GGAPIEntityHelper.getFieldValue(entityClass, this.passwordField, entity);
		List<String> authorities = (List<String>) GGAPIEntityHelper.getFieldValue(entityClass, this.authoritiesField, entity);
		boolean isAccountNonExpired = (boolean) GGAPIEntityHelper.getFieldValue(entityClass, this.isAccountNonExpiredField, entity);
		boolean isAccountNonLocked = (boolean) GGAPIEntityHelper.getFieldValue(entityClass, this.isAccountNonLockedField, entity);
		boolean isCredentialsNonExpired = (boolean) GGAPIEntityHelper.getFieldValue(entityClass, this.isCredentialsNonExpiredField, entity);
		boolean isEnabled = (boolean) GGAPIEntityHelper.getFieldValue(entityClass, this.isEnabledField, entity);
		
		GGAPIEntityAuthenticator authenticator = new GGAPIEntityAuthenticator(password, authorities, login, isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired, isEnabled, entity.getUuid(), tenantId, entity, null);
		GGAPIEntityAuthentication authentication = new GGAPIEntityAuthentication(authenticator);
		return authenticator;
	}

	@Override
	public IGGAPIAuthenticator getAuthenticatorFromEntity(String tenantId, IGGAPIEntity entity) throws GGAPIEntityException {
		
		try {
			return (IGGAPIAuthenticator) this.getAuthenticatorFromEntity(tenantId, entity.getClass(), entity);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new GGAPIEntityException(e);
		}
	}
}
