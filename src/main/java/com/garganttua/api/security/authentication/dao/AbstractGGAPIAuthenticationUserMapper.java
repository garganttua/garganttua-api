package com.garganttua.api.security.authentication.dao;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.garganttua.api.security.GGAPISecurityException;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;

public abstract class AbstractGGAPIAuthenticationUserMapper<UserEntity extends IGGAPIAuthenticator> implements IGGAPIAuthenticationUserMapper {

	@Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
    	
		UserEntity userEntity = null;
		try {
			userEntity = this.getEntity(login);
		} catch (GGAPISecurityException e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
	
		return this.mapUser(userEntity);
    }

	protected abstract UserEntity getEntity(String login) throws GGAPISecurityException;
	protected abstract UserDetails mapUser(UserEntity entity);
	
}
