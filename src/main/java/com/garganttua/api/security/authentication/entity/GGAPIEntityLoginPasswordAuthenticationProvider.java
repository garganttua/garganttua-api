package com.garganttua.api.security.authentication.entity;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authentication.dao.IGGAPIAuthenticationUserMapper;
import com.garganttua.api.security.authentication.modes.loginpassword.IGGAPILoginPasswordAuthenticationEntity;
import com.garganttua.api.spec.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIReadOutputMode;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.filter.GGAPILiteral;

public class GGAPIEntityLoginPasswordAuthenticationProvider implements IGGAPIAuthenticationUserMapper {

	private String magicTenantId;
	private String loginField;
	private IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> controller;

	public GGAPIEntityLoginPasswordAuthenticationProvider(
			IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> controller, String magicTenantId, String loginField) {
				this.controller = controller;
				this.magicTenantId = magicTenantId;
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
			List<IGGAPILoginPasswordAuthenticationEntity> entities = (List<IGGAPILoginPasswordAuthenticationEntity>) this.controller.getEntityList(this.magicTenantId, null, 0, 0, filter, null, null, GGAPIReadOutputMode.full);
			
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
