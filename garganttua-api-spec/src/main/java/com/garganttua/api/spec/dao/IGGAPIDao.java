/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.spec.dao;

import java.util.List;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.sort.IGGAPISort;

public interface IGGAPIDao<Dto> extends IGGAPIEngineObject {
	
	void setDtoClass(Class<Dto> dtoClass);
	
	List<Dto> find(IGGAPIPageable pageable, IGGAPIFilter filter, IGGAPISort sort) throws GGAPIException;

	Dto save(Dto object) throws GGAPIException;

	void delete(Dto object) throws GGAPIException;
	
	long count(IGGAPIFilter filter) throws GGAPIException;
}
