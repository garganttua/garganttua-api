/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.spec.repository;

import java.util.List;
import java.util.Optional;

import org.javatuples.Pair;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.engine.IEngineObject;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;

/**
 * 
 * @author JérémyCOLOMBET
 *
 * @param <Entity>
 */
public interface IRepository extends IEngineObject {

	boolean doesExist(ICaller caller, Object entity) throws CoreException;

	List<Object> getEntities(ICaller caller, IPageable pageable, IFilter filter, ISort sort) throws CoreException;

	void save(ICaller caller, Object entity) throws CoreException;

	Optional<Object> getOneById(ICaller caller, String id) throws CoreException;

	void delete(ICaller caller, Object entity) throws CoreException;

	boolean doesExist(ICaller caller, String uuid) throws CoreException;
	
	Optional<Object> getOneByUuid(ICaller caller, String uuid) throws CoreException;

	long getCount(ICaller caller, IFilter filter) throws CoreException;

	String getTenant(Object entity) throws CoreException;

	void setDaos(List<Pair<Class<?>, IDao<?>>> daos);

}
