package com.garganttua.api.security.core.entity.checker;

import java.util.List;

import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAccountNonExpired;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAccountNonLocked;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorCredentialsNonExpired;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorEnabled;
import com.garganttua.api.spec.security.annotations.GGAPIEntitySecurity;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

@GGAPIEntitySecurity(
		creation_access = GGAPIServiceAccess.anonymous,
		read_all_access = GGAPIServiceAccess.anonymous,
		read_one_access = GGAPIServiceAccess.anonymous,
		update_one_access = GGAPIServiceAccess.anonymous,
		delete_all_access = GGAPIServiceAccess.anonymous,
		delete_one_access = GGAPIServiceAccess.anonymous,
		count_access = GGAPIServiceAccess.anonymous,
		creation_authority = true,
		read_all_authority = true,
		read_one_authority = true,
		update_one_authority = true,
		delete_all_authority = true,
		delete_one_authority = true,
		count_authority = true
)
@GGAPIAuthenticator
public class TestAuthenticator {
	
	@GGAPIAuthenticatorAccountNonExpired
	@GGAPIAuthenticatorAccountNonLocked
	@GGAPIAuthenticatorCredentialsNonExpired
	@GGAPIAuthenticatorEnabled
	private boolean bool;
	
	@GGAPIAuthenticatorAuthorities
	private List<String> list;

}
