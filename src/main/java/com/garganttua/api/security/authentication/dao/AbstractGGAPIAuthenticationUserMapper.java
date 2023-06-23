package com.garganttua.api.security.authentication.dao;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.garganttua.api.security.authentication.IGGAPISecurityException;
import com.garganttua.api.spec.IGGAPIEntity;

public abstract class AbstractGGAPIAuthenticationUserMapper<UserEntity extends IGGAPIEntity> implements IGGAPIAuthenticationUserMapper {

	@Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
    	
		UserEntity userEntity = null;
		try {
			userEntity = this.getEntity(login);
		} catch (IGGAPISecurityException e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
	
		return this.mapUser(userEntity);
    }

	protected abstract UserEntity getEntity(String login) throws IGGAPISecurityException;
	protected abstract UserDetails mapUser(UserEntity entity);
	
}
