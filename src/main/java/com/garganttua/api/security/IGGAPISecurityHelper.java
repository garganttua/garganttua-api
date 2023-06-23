package com.garganttua.api.security;

import java.util.List;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.garganttua.api.security.authentication.IGGAPISecurityException;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;

public interface IGGAPISecurityHelper {

	HttpSecurity configureFilterChain(HttpSecurity http) throws IGGAPISecurityException;

	List<IGGAPIAuthorization> getAuthorizations();

}
