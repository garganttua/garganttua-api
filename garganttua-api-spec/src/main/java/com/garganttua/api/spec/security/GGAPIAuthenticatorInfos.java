package com.garganttua.api.spec.security;

import java.util.Objects;

import com.garganttua.reflection.GGObjectAddress;

public record GGAPIAuthenticatorInfos (
		GGObjectAddress authoritiesFieldAddress,
		GGObjectAddress isAccountNonExpiredFieldAddress,
		GGObjectAddress isAccountNonLockedFieldAddress,
		GGObjectAddress isCredentialsNonExpiredFieldAddress,
		GGObjectAddress isEnabledFieldAddress) {
		
		@Override
	    public int hashCode() {
	        return Objects.hash(
	                authoritiesFieldAddress,
	                isAccountNonExpiredFieldAddress,
	                isAccountNonLockedFieldAddress,
	                isCredentialsNonExpiredFieldAddress,
	                isEnabledFieldAddress);
	    }

	    @Override
	    public String toString() {
	        return "GGAPIAuthenticatorInfos{" +
	                "authoritiesFieldAddress=" + authoritiesFieldAddress +
	                ", isAccountNonExpiredFieldAddress=" + isAccountNonExpiredFieldAddress +
	                ", isAccountNonLockedFieldAddress=" + isAccountNonLockedFieldAddress +
	                ", isCredentialsNonExpiredFieldAddress=" + isCredentialsNonExpiredFieldAddress +
	                ", isEnabledField=" + isEnabledFieldAddress +
	                '}';
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        GGAPIAuthenticatorInfos that = (GGAPIAuthenticatorInfos) o;
	        return Objects.equals(authoritiesFieldAddress, that.authoritiesFieldAddress) &&
	                Objects.equals(isAccountNonExpiredFieldAddress, that.isAccountNonExpiredFieldAddress) &&
	                Objects.equals(isAccountNonLockedFieldAddress, that.isAccountNonLockedFieldAddress) &&
	                Objects.equals(isCredentialsNonExpiredFieldAddress, that.isCredentialsNonExpiredFieldAddress) &&
	                Objects.equals(isEnabledFieldAddress, that.isEnabledFieldAddress);
	    }
	}
