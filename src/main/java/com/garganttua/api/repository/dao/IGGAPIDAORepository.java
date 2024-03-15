/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.IGGAPIEngineObject;

public interface IGGAPIDAORepository<Dto> extends IGGAPIEngineObject {
	
	void setDtoClass(Class<Dto> dtoClass);
	
	List<Dto> find(Pageable pageable, GGAPILiteral filter, GGAPISort sort) throws GGAPIDaoException;

	Dto save(Dto object) throws GGAPIDaoException;

	void delete(Dto object) throws GGAPIDaoException;
	
	long count(GGAPILiteral filter) throws GGAPIDaoException;
}
