/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.spec.dao;

import java.util.List;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IEngineObject;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;

public interface IDao<Dto> extends IEngineObject {
	
	void setDtoClass(Class<Dto> dtoClass);
	
	List<Dto> find(IPageable pageable, IFilter filter, ISort sort) throws CoreException;

	Dto save(Dto object) throws CoreException;

	void delete(Dto object) throws CoreException;
	
	long count(IFilter filter) throws CoreException;
}
