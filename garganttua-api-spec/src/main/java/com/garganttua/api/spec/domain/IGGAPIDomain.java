package com.garganttua.api.spec.domain;

import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.api.spec.entity.GGAPIEntityDocumentationInfos;
import com.garganttua.api.spec.entity.GGAPIEntityInfos;
import com.garganttua.api.spec.security.GGAPIEntitySecurityInfos;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

public interface IGGAPIDomain {

	String getDomain();

	Pair<Class<?>, GGAPIEntityInfos> getEntity();

	List<Pair<Class<?>, GGAPIDtoInfos>> getDtos();
	
	GGAPIEntitySecurityInfos getSecurity();

	String[] getInterfaces();

	String getEvent();

	boolean isAllowCreation();

	boolean isAllowReadAll();

	boolean isAllowReadOne();

	boolean isAllowUpdateOne();

	boolean isAllowDeleteOne();

	boolean isAllowDeleteAll();

//	boolean isAllowCount();

	boolean isTenantIdMandatoryForOperation(GGAPIEntityOperation operation);

	boolean isOwnerIdMandatoryForOperation(GGAPIEntityOperation operation);

	GGAPIEntityDocumentationInfos getDocumentation();

	void addServicesInfos(List<IGGAPIServiceInfos> servicesInfos);
	
	String getEntityName();
	
	Map<GGAPIEntityOperation, IGGAPIServiceInfos> getServiceInfos();

	void addServiceInfos(IGGAPIServiceInfos servicesInfos);

	IGGAPIAccessRule createAccessRule(IGGAPIServiceInfos info) throws GGAPIException;

}
