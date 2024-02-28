/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIDAORepository extends IGGAPIEngineObject {
	
	void init(List<GGAPIDynamicDomain> domains);

	List<? extends IGGAPIDTOObject<? extends IGGAPIEntity>> find(GGAPIDynamicDomain domain, Pageable pageable, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc);

	<Dto extends IGGAPIDTOObject<? extends IGGAPIEntity>> Dto save(Dto object);

	<Dto extends IGGAPIDTOObject<? extends IGGAPIEntity>> void delete(Dto object);
	
	long count(GGAPIDynamicDomain domain, GGAPILiteral filter, GGAPIGeolocFilter geoloc);

}
