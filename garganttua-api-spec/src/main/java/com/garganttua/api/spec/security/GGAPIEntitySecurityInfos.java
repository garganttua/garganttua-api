package com.garganttua.api.spec.security;

import java.util.Objects;

import com.garganttua.api.spec.service.GGAPIServiceAccess;

public record GGAPIEntitySecurityInfos (
		GGAPIServiceAccess creationAccess,
		GGAPIServiceAccess readAllAccess,
		GGAPIServiceAccess readOneAccess,
		GGAPIServiceAccess updateOneAccess,
		GGAPIServiceAccess deleteOneAccess,
		GGAPIServiceAccess deleteAllAccess,
		GGAPIServiceAccess countAccess,
		boolean creationAuthority,
		boolean readAllAuthority,
		boolean readOneAuthority,
		boolean updateOneAuthority,
		boolean deleteOneAuthority,
		boolean deleteAllAuthority,
		boolean countAuthority, 
		GGAPIAuthenticatorInfos authenticatorInfos
		) {
	 @Override
	    public String toString() {
	        return "GGAPIEntitySecurityInfos{" +
	                "creationAccess=" + creationAccess +
	                ", readAllAccess=" + readAllAccess +
	                ", readOneAccess=" + readOneAccess +
	                ", updateOneAccess=" + updateOneAccess +
	                ", deleteOneAccess=" + deleteOneAccess +
	                ", deleteAllAccess=" + deleteAllAccess +
	                ", countAccess=" + countAccess +
	                ", creationAuthority=" + creationAuthority +
	                ", readAllAuthority=" + readAllAuthority +
	                ", readOneAuthority=" + readOneAuthority +
	                ", updateOneAuthority=" + updateOneAuthority +
	                ", deleteAuthority=" + deleteOneAuthority +
	                ", deleteAllAuthority=" + deleteAllAuthority +
	                ", countAuthority=" + countAuthority +
	                ", authenticatorInfos=" + authenticatorInfos +
	                '}';
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        GGAPIEntitySecurityInfos that = (GGAPIEntitySecurityInfos) o;
	        return creationAuthority == that.creationAuthority &&
	                readAllAuthority == that.readAllAuthority &&
	                readOneAuthority == that.readOneAuthority &&
	                updateOneAuthority == that.updateOneAuthority &&
	                deleteOneAuthority == that.deleteOneAuthority &&
	                deleteAllAuthority == that.deleteAllAuthority &&
	                countAuthority == that.countAuthority &&
	                Objects.equals(creationAccess, that.creationAccess) &&
	                Objects.equals(readAllAccess, that.readAllAccess) &&
	                Objects.equals(readOneAccess, that.readOneAccess) &&
	                Objects.equals(updateOneAccess, that.updateOneAccess) &&
	                Objects.equals(deleteOneAccess, that.deleteOneAccess) &&
	                Objects.equals(deleteAllAccess, that.deleteAllAccess) &&
	                Objects.equals(countAccess, that.countAccess) &&
	                Objects.equals(authenticatorInfos, that.authenticatorInfos);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(
	                creationAccess,
	                readAllAccess,
	                readOneAccess,
	                updateOneAccess,
	                deleteOneAccess,
	                deleteAllAccess,
	                countAccess,
	                creationAuthority,
	                readAllAuthority,
	                readOneAuthority,
	                updateOneAuthority,
	                deleteOneAuthority,
	                deleteAllAuthority,
	                countAuthority, 
	                authenticatorInfos
	        );
	    }

		public boolean isAuthenticatorEntity() {
			return this.authenticatorInfos==null?false:true;
		}
}
