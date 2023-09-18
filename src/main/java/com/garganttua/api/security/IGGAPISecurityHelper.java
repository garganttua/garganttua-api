package com.garganttua.api.security;

import java.util.List;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.garganttua.api.security.authorization.IGGAPIAuthorization;

public interface IGGAPISecurityHelper {

	HttpSecurity configureFilterChain(HttpSecurity http);

	List<IGGAPIAuthorization> getAuthorizations();

}
