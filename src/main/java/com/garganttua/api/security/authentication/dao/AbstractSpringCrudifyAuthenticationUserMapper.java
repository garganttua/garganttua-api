package com.garganttua.api.security.authentication.dao;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.garganttua.api.security.authentication.ISpringCrudifySecurityException;
import com.garganttua.api.spec.ISpringCrudifyEntity;

public abstract class AbstractSpringCrudifyAuthenticationUserMapper<UserEntity extends ISpringCrudifyEntity> implements ISpringCrudifyAuthenticationUserMapper {

	@Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
    	
		UserEntity userEntity = null;
		try {
			userEntity = this.getEntity(login);
		} catch (ISpringCrudifySecurityException e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
	
		return this.mapUser(userEntity);
    }

	protected abstract UserEntity getEntity(String login) throws ISpringCrudifySecurityException;
	protected abstract UserDetails mapUser(UserEntity entity);
	
}
