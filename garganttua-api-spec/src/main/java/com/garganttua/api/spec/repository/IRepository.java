/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.spec.repository;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.CoreException;

/**
 * 
 * @author JérémyCOLOMBET
 *
 */
public interface IRepository {

	boolean doesExist(Object entity) throws CoreException;

	List<Object> getEntities(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
			throws CoreException;

	void save(Object entity) throws CoreException;

	Optional<Object> getOneById(String id) throws CoreException;

	void delete(Object entity) throws CoreException;

	boolean doesExist(String uuid) throws CoreException;

	Optional<Object> getOneByUuid(String uuid) throws CoreException;

	long getCount(IFilter filter) throws CoreException;

}
