package com.garganttua.api.security;

import java.util.List;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.garganttua.api.security.authentication.ISpringCrudifySecurityException;
import com.garganttua.api.security.authorization.ISpringCrudifyAuthorization;

public interface ISpringCrudifySecurityHelper {

	HttpSecurity configureFilterChain(HttpSecurity http) throws ISpringCrudifySecurityException;

	List<ISpringCrudifyAuthorization> getAuthorizations();

}
