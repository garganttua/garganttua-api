package com.garganttua.api.spec.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.api.spec.entity.GGAPIEntityDocumentationInfos;
import com.garganttua.api.spec.entity.annotations.GGAPIUnicityScope;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorScope;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.reflection.GGObjectAddress;

public interface IGGAPIDomain {

	String getDomain();

	List<Pair<Class<?>, GGAPIDtoInfos>> getDtos();

	String[] getInterfaces();

	String getEvent();

	boolean isAllowCreation();

	boolean isAllowReadAll();

	boolean isAllowReadOne();

	boolean isAllowUpdateOne();

	boolean isAllowDeleteOne();

	boolean isAllowDeleteAll();

	boolean isTenantIdMandatoryForOperation(GGAPIEntityOperation operation);

	boolean isOwnerIdMandatoryForOperation(GGAPIEntityOperation operation);

	GGAPIEntityDocumentationInfos getDocumentation();

	void addServicesInfos(List<IGGAPIServiceInfos> servicesInfos);
	
	String getEntityName();
	
	void addServiceInfos(IGGAPIServiceInfos servicesInfos);

	Collection<IGGAPIAccessRule> getAccessRules();

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

	List<Pair<GGObjectAddress, GGAPIUnicityScope>> getUnicityFields();

	List<GGObjectAddress> getMandatoryFields();

	IGGAPIAccessRule createAccessRule(IGGAPIServiceInfos serviceInfos) throws GGAPIException;

	Collection<Class<?>> getAuthorizationProtocols();

	Collection<Class<?>> getAuthorizations();

	GGAPIServiceAccess getAccess(IGGAPIServiceInfos info);

	String getAuthority(IGGAPIServiceInfos info);

	Map<GGAPIEntityOperation, IGGAPIServiceInfos> getServiceInfos();

	GGAPIAuthenticatorScope getAuthenticatorScope();
}
