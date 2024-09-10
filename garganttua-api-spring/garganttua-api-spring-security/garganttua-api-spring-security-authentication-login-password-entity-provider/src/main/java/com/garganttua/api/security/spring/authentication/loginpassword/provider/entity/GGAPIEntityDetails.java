package com.garganttua.api.security.spring.authentication.loginpassword.provider.entity;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.garganttua.api.security.core.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.spec.GGAPIException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityDetails implements UserDetails {

	private static final long serialVersionUID = -905400933897718679L;
	private Object entity;

	public GGAPIEntityDetails(Object entity) {
		this.entity = entity;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		try {
			List<String> authorities = GGAPIEntityAuthenticatorHelper.getAuthorities(this.entity);
			
			return authorities.stream().map(
				(
					authority
				) 
				-> 
				{
					return new GrantedAuthority() {
						
						private static final long serialVersionUID = -8283536681606086962L;
		
						@Override
						public String getAuthority() {
							return authority;
						}
					};
				}).collect(Collectors.toList());

		} catch (GGAPIException e) {
			if( log.isDebugEnabled() ) {
				log.warn("Unable to get authorities", e);
			}
			return null;
		}
	}

	@Override
	public String getPassword() {
		try {
			return GGAPIEntityAuthenticatorHelper.getPassword(this.entity);
		} catch (GGAPIException e) {
			if( log.isDebugEnabled() ) {
				log.warn("Unable to get password", e);
			}
			return null;
		}
	}

	@Override
	public String getUsername() {
		try {
			return GGAPIEntityAuthenticatorHelper.getLogin(this.entity);
		} catch (GGAPIException e) {
			if( log.isDebugEnabled() ) {
				log.warn("Unable to get login", e);
			}
			return null;
		}
	}

	@Override
	public boolean isAccountNonExpired() {
		try {
			return GGAPIEntityAuthenticatorHelper.isAccountNonExpired(this.entity);
		} catch (GGAPIException e) {
			if( log.isDebugEnabled() ) {
				log.warn("Unable to get isAccountNonExpired", e);
			}
			return false;
		}
	}

	@Override
	public boolean isAccountNonLocked() {
		try {
			return GGAPIEntityAuthenticatorHelper.isAccountNonLocked(this.entity);
		} catch (GGAPIException e) {
			if( log.isDebugEnabled() ) {
				log.warn("Unable to get isAccountNonLocked", e);
			}
			return false;
		}
	}

	@Override
	public boolean isCredentialsNonExpired() {
		try {
			return GGAPIEntityAuthenticatorHelper.isCredentialsNonExpired(this.entity);
		} catch (GGAPIException e) {
			if( log.isDebugEnabled() ) {
				log.warn("Unable to get isCredentialsNonExpired", e);
			}
			return false;
		}
	}

	@Override
	public boolean isEnabled() {
		try {
			return GGAPIEntityAuthenticatorHelper.isEnabled(this.entity);
		} catch (GGAPIException e) {
			if( log.isDebugEnabled() ) {
				log.warn("Unable to get isEnabled", e);
			}
			return false;
		}
	}

}
