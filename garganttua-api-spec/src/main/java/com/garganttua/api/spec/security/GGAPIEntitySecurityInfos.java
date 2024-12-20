package com.garganttua.api.spec.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GGAPIEntitySecurityInfos {

	private GGAPIServiceAccess creationAccess;
	private GGAPIServiceAccess readAllAccess;
	private GGAPIServiceAccess readOneAccess;
	private GGAPIServiceAccess updateOneAccess;
	private GGAPIServiceAccess deleteOneAccess;
	private GGAPIServiceAccess deleteAllAccess;
	private GGAPIServiceAccess countAccess;
	private boolean creationAuthority;
	private boolean readAllAuthority;
	private boolean readOneAuthority;
	private boolean updateOneAuthority;
	private boolean deleteOneAuthority;
	private boolean deleteAllAuthority;
	private boolean countAuthority;
	private boolean isAuthenticatorEntity;
	private boolean isAuthorizationEntity;
	private Class<?>[] authorizations;
	private Class<?>[] authorizationProtocols;
	private String domainName;

	private Map<GGAPIEntityOperation, IGGAPIAccessRule> accessRules = new HashMap<GGAPIEntityOperation, IGGAPIAccessRule>();

	public void addAccessRules(List<IGGAPIAccessRule> rules) {
		rules.forEach(rule -> {
			this.addAccessRule(rule);
		});
	}
	
	public void addAccessRule(IGGAPIAccessRule rule) {
		this.accessRules.put(rule.getOperation(), rule);
	}

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
                ", deleteOneAuthority=" + deleteOneAuthority +
                ", deleteAllAuthority=" + deleteAllAuthority +
                ", countAuthority=" + countAuthority +
                ", isAuthenticatorEntity=" + isAuthenticatorEntity +
                ", isAuthorizationEntity=" + isAuthorizationEntity +
                ", authorizations=" + Arrays.toString(authorizations) +
                ", authorizationProtocols=" + Arrays.toString(authorizationProtocols) +
                ", domainName='" + domainName + '\'' +
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
                isAuthenticatorEntity == that.isAuthenticatorEntity &&
                isAuthorizationEntity == that.isAuthorizationEntity &&
                Objects.equals(creationAccess, that.creationAccess) &&
                Objects.equals(readAllAccess, that.readAllAccess) &&
                Objects.equals(readOneAccess, that.readOneAccess) &&
                Objects.equals(updateOneAccess, that.updateOneAccess) &&
                Objects.equals(deleteOneAccess, that.deleteOneAccess) &&
                Objects.equals(deleteAllAccess, that.deleteAllAccess) &&
                Objects.equals(countAccess, that.countAccess) &&
                Arrays.equals(authorizations, that.authorizations) &&
                Arrays.equals(authorizationProtocols, that.authorizationProtocols) &&
                Objects.equals(domainName, that.domainName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(creationAccess, readAllAccess, readOneAccess, updateOneAccess, deleteOneAccess, deleteAllAccess, countAccess,
                creationAuthority, readAllAuthority, readOneAuthority, updateOneAuthority, deleteOneAuthority, deleteAllAuthority,
                countAuthority, isAuthenticatorEntity, isAuthorizationEntity, domainName);
        result = 31 * result + Arrays.hashCode(authorizations)+ Arrays.hashCode(authorizationProtocols);
        return result;
    }

	public String getAuthority(IGGAPIServiceInfos infos) {
		IGGAPIAccessRule rule = this.accessRules.get(infos.getOperation());
		if (rule != null)
			return rule.getAuthority();

		return null;
	}

	public Class<?>[] getAuthorizations(){
		return this.authorizations;
	}
	
	public GGAPIServiceAccess getAccess(IGGAPIServiceInfos infos) {
		IGGAPIAccessRule rule = this.accessRules.get(infos.getOperation());
		if (rule != null)
			return rule.getAccess();

		return GGAPIServiceAccess.tenant;
	}

	public GGAPIEntitySecurityInfos(GGAPIServiceAccess creationAccess, GGAPIServiceAccess readAllAccess,
			GGAPIServiceAccess readOneAccess, GGAPIServiceAccess updateOneAccess, GGAPIServiceAccess deleteOneAccess,
			GGAPIServiceAccess deleteAllAccess, GGAPIServiceAccess countAccess, boolean creationAuthority,
			boolean readAllAuthority, boolean readOneAuthority, boolean updateOneAuthority, boolean deleteOneAuthority,
			boolean deleteAllAuthority, boolean countAuthority, boolean isAuthenticatorEntity, boolean isAuthorizationEntity,
			Class<?>[] authorizations, Class<?>[] authorizationProtocols,
			String domainName) {
		this.creationAccess = creationAccess;
		this.readAllAccess = readAllAccess;
		this.readOneAccess = readOneAccess;
		this.updateOneAccess = updateOneAccess;
		this.deleteOneAccess = deleteOneAccess;
		this.deleteAllAccess = deleteAllAccess;
		this.countAccess = countAccess;
		this.creationAuthority = creationAuthority;
		this.readAllAuthority = readAllAuthority;
		this.readOneAuthority = readOneAuthority;
		this.updateOneAuthority = updateOneAuthority;
		this.deleteOneAuthority = deleteOneAuthority;
		this.deleteAllAuthority = deleteAllAuthority;
		this.countAuthority = countAuthority;
		this.isAuthenticatorEntity = isAuthenticatorEntity;
		this.isAuthorizationEntity = isAuthorizationEntity;
		this.authorizations = authorizations;
		this.authorizationProtocols = authorizationProtocols;
		this.domainName = domainName;
	}

}
