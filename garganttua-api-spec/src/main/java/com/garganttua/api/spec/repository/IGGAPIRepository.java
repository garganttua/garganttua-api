/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.spec.repository;

import java.util.List;
import java.util.Optional;

import org.javatuples.Pair;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.dao.IGGAPIDao;
import com.garganttua.api.spec.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.sort.IGGAPISort;

/**
 * 
 * @author JérémyCOLOMBET
 *
 * @param <Entity>
 */
public interface IGGAPIRepository extends IGGAPIEngineObject {

	boolean doesExist(IGGAPICaller caller, Object entity) throws GGAPIException;

	List<Object> getEntities(IGGAPICaller caller, IGGAPIPageable pageable, IGGAPIFilter filter, IGGAPISort sort) throws GGAPIException;

	void save(IGGAPICaller caller, Object entity) throws GGAPIException;

	Optional<Object> getOneById(IGGAPICaller caller, String id) throws GGAPIException;

	void delete(IGGAPICaller caller, Object entity) throws GGAPIException;

	boolean doesExist(IGGAPICaller caller, String uuid) throws GGAPIException;
	
	Optional<Object> getOneByUuid(IGGAPICaller caller, String uuid) throws GGAPIException;

	long getCount(IGGAPICaller caller, IGGAPIFilter filter) throws GGAPIException;

	String getTenant(Object entity) throws GGAPIException;

	void setDaos(List<Pair<Class<?>, IGGAPIDao<?>>> daos);

}
