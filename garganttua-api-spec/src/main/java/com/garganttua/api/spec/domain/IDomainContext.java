package com.garganttua.api.spec.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.dto.DtoInfos;
import com.garganttua.api.spec.entity.EntityDocumentationInfos;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.service.IServiceInfos;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.ObjectAddress;

public interface IDomainContext<E> {

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

	boolean isTenantIdMandatoryForOperation(Operation operation);

	boolean isOwnerIdMandatoryForOperation(Operation operation);

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

	ObjectAddress getSuperOnwerIdFieldAddress();

	ObjectAddress getSuperTenantFieldAddress();

	ObjectAddress getBeforeDeleteMethodAddress();

	ObjectAddress getAfterDeleteMethodAddress();

	ObjectAddress getBeforeCreateMethodAddress();

	ObjectAddress getAfterCreateMethodAddress();

	ObjectAddress getBeforeUpdateMethodAddress();

	ObjectAddress getAfterUpdateMethodAddress();

	ObjectAddress getOwnerIdFieldAddress();
	
	ObjectAddress getAfterGetMethodAddress();

	ObjectAddress getShareFieldAddress();

	ObjectAddress getTenantIdFieldAddress();

	ObjectAddress getHiddenFieldAddress();
	
	ObjectAddress getUuidFieldAddress();
	
	ObjectAddress getIdFieldAddress();

	ObjectAddress getLocationFieldAddress();

	Map<ObjectAddress, String> getAuthorizedUpdateFieldsAndAuthorizations();

	List<Pair<ObjectAddress, UnicityScope>> getUnicityFields();

	List<ObjectAddress> getMandatoryFields();

	IAccessRule createAccessRule(IServiceInfos serviceInfos) throws CoreException;

	Collection<Class<?>> getAuthorizationProtocols();

	Collection<Class<?>> getAuthorizations();

	Access getAccess(IServiceInfos info);

	String getAuthority(IServiceInfos info);

	Map<Operation, IServiceInfos> getServiceInfos();

	AuthenticatorScope getAuthenticatorScope();
}
