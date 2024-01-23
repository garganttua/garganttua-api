package com.garganttua.api.security.authentication.entity;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.GGAPIReadOutputMode;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authentication.dao.IGGAPIAuthenticationUserMapper;
import com.garganttua.api.security.authentication.modes.loginpassword.IGGAPILoginPasswordAuthenticationEntity;

public class GGAPIEntityLoginPasswordAuthenticationProvider implements IGGAPIAuthenticationUserMapper {

	private String superTenantId;
	private String loginField;
	private IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> controller;

	public GGAPIEntityLoginPasswordAuthenticationProvider(
			IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> controller, String superTenantId, String loginField) {
				this.controller = controller;
				this.superTenantId = superTenantId;
				this.loginField = loginField;
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
			List<IGGAPILoginPasswordAuthenticationEntity> entities = (List<IGGAPILoginPasswordAuthenticationEntity>) this.controller.getEntityList(caller, 0, 0, filter, null, null, GGAPIReadOutputMode.full);
			
			if( entities.size() < 1 ) {
				throw new UsernameNotFoundException("Login "+login+" not found");
			}
			
			userDetails = entities.get(0);
			
		} catch (GGAPIEntityException e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
		return userDetails;
	}
}
