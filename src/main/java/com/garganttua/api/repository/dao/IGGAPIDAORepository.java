/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.garganttua.api.core.IGGAPIDomainable;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIDAORepository<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto>, IGGAPIEngineObject{

	List<Dto> find(Pageable pageable, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc);

	Dto save(Dto object);

	void delete(Dto object);
	
	long count(GGAPILiteral filter, GGAPIGeolocFilter geoloc);

}
