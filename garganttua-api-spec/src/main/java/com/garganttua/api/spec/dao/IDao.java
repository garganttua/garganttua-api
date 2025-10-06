/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.spec.dao;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IEngineObject;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;

public interface IDao extends IEngineObject {
	
	void setDtoClass(Class<?> dtoClass);
	
	List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort) throws CoreException;

	Object save(Object object) throws CoreException;

	void delete(Object object) throws CoreException;
	
	long count(IFilter filter) throws CoreException;
}
