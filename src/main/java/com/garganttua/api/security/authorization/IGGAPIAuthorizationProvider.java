package com.garganttua.api.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.garganttua.api.security.keys.GGAPIKeyExpiredException;

public interface IGGAPIAuthorizationProvider {

	String getAuthorization(Authentication authentication) throws GGAPIKeyExpiredException;

	String getUserNameFromAuthorization(String token) throws GGAPIKeyExpiredException;

	boolean validateAuthorization(String token, UserDetails userDetails) throws GGAPIKeyExpiredException;

}
