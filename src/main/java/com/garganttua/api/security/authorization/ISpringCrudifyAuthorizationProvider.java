package com.garganttua.api.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.garganttua.api.security.keys.SpringCrudifyKeyExpiredException;

public interface ISpringCrudifyAuthorizationProvider {

	String getAuthorization(Authentication authentication) throws SpringCrudifyKeyExpiredException;

	String getUserNameFromAuthorization(String token) throws SpringCrudifyKeyExpiredException;

	boolean validateAuthorization(String token, UserDetails userDetails) throws SpringCrudifyKeyExpiredException;

}
