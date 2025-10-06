package com.garganttua.api.spec.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.dto.DtoInfos;
import com.garganttua.api.spec.entity.EntityDocumentationInfos;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.api.spec.service.IServiceInfos;
import com.garganttua.reflection.GGObjectAddress;

public interface  IDomain {

	String getDomain();

	List<Pair<Class<?>, DtoInfos>> getDtos();

	String[] getInterfaces();

	String getEvent();

	boolean isAllowCreation();

	boolean isAllowReadAll();

	boolean isAllowReadOne();

	boolean isAllowUpdateOne();

	boolean isAllowDeleteOne();

	boolean isAllowDeleteAll();

	boolean isTenantIdMandatoryForOperation(EntityOperation operation);

	boolean isOwnerIdMandatoryForOperation(EntityOperation operation);

	EntityDocumentationInfos getDocumentation();

	void addServicesInfos(List<IServiceInfos> servicesInfos);
	
	String getEntityName();
	
	void addServiceInfos(IServiceInfos servicesInfos);

	Collection<IAccessRule> getAccessRules();

	Collection<String> getUpdateAuthorizations();

	Class<?> getEntityClass();

	boolean isOwnerEntity();

	boolean isTenantEntity();

	boolean isOwnedEntity();

	boolean isSharedEntity();

	boolean isHiddenableEntity();

	boolean isPublicEntity();
	
	boolean isGeolocalizedEntity();
	
	boolean isAuthenticatorEntity();

	GGObjectAddress getSuperOnwerIdFieldAddress();

	GGObjectAddress getSuperTenantFieldAddress();

	GGObjectAddress getBeforeDeleteMethodAddress();

	GGObjectAddress getAfterDeleteMethodAddress();

	GGObjectAddress getBeforeCreateMethodAddress();

	GGObjectAddress getAfterCreateMethodAddress();

	GGObjectAddress getBeforeUpdateMethodAddress();

	GGObjectAddress getAfterUpdateMethodAddress();

	GGObjectAddress getOwnerIdFieldAddress();
	
	GGObjectAddress getAfterGetMethodAddress();

	GGObjectAddress getShareFieldAddress();

	GGObjectAddress getTenantIdFieldAddress();

	GGObjectAddress getHiddenFieldAddress();
	
	GGObjectAddress getUuidFieldAddress();
	
	GGObjectAddress getIdFieldAddress();

	GGObjectAddress getLocationFieldAddress();

	Map<GGObjectAddress, String> getAuthorizedUpdateFieldsAndAuthorizations();

	List<Pair<GGObjectAddress, UnicityScope>> getUnicityFields();

	List<GGObjectAddress> getMandatoryFields();

	IAccessRule createAccessRule(IServiceInfos serviceInfos) throws CoreException;

	Collection<Class<?>> getAuthorizationProtocols();

	Collection<Class<?>> getAuthorizations();

	ServiceAccess getAccess(IServiceInfos info);

	String getAuthority(IServiceInfos info);

	Map<EntityOperation, IServiceInfos> getServiceInfos();

	AuthenticatorScope getAuthenticatorScope();
}
