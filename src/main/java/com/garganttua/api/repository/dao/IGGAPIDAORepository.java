/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.garganttua.api.business.IGGAPIBusiness;
import com.garganttua.api.connector.IGGAPIConnector;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIDomainable;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.filter.GGAPIGeolocFilter;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.sort.GGAPISort;

public interface IGGAPIDAORepository<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto>, IGGAPIEngineObject{

	List<Dto> findByTenantId(String tenantId, Pageable pageable, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc);

	Dto findOneByUuidAndTenantId(String uuid, String tenantId);

	Dto findOneByIdAndTenantId(String id, String tenantId);

	Dto save(Dto object);

	void delete(Dto object);
	
	long countByTenantId(String tenantId, GGAPILiteral filter);

	void setMagicTenantId(String magicTenantId);
	
	void setHiddenable(boolean hiddenable);
	
	void setPublic(boolean publicEntity);
	
	void setShared(String sharingField);
	
	String getMagicTenantId();

	void setGeolocalized(String geolocField);

}
