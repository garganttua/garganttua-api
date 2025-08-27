package com.garganttua.api.core.security.entity.checker;

import java.util.List;

import com.garganttua.api.spec.entity.annotations.EntityOwner;
import com.garganttua.api.spec.security.annotations.Authenticator;
import com.garganttua.api.spec.security.annotations.AuthenticatorAccountNonExpired;
import com.garganttua.api.spec.security.annotations.AuthenticatorAccountNonLocked;
import com.garganttua.api.spec.security.annotations.AuthenticatorAuthorities;
import com.garganttua.api.spec.security.annotations.AuthenticatorCredentialsNonExpired;
import com.garganttua.api.spec.security.annotations.AuthenticatorEnabled;
import com.garganttua.api.spec.security.annotations.EntitySecurity;
import com.garganttua.api.spec.service.ServiceAccess;

@EntitySecurity(
		creation_access = ServiceAccess.anonymous,
		read_all_access = ServiceAccess.anonymous,
		read_one_access = ServiceAccess.anonymous,
		update_one_access = ServiceAccess.anonymous,
		delete_all_access = ServiceAccess.anonymous,
		delete_one_access = ServiceAccess.anonymous,
		count_access = ServiceAccess.anonymous,
		creation_authority = true,
		read_all_authority = true,
		read_one_authority = true,
		update_one_authority = true,
		delete_all_authority = true,
		delete_one_authority = true,
		count_authority = true
)
@Authenticator
@EntityOwner
public class TestAuthenticator {
	
	@AuthenticatorAccountNonExpired
	@AuthenticatorAccountNonLocked
	@AuthenticatorCredentialsNonExpired
	@AuthenticatorEnabled
	private Boolean bool;
	
	@AuthenticatorAuthorities
	private List<String> list;

}
