/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.spec.repository;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.IGGAPICaller;
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
public interface IGGAPIRepository<Entity> extends IGGAPIEngineObject {

	boolean doesExist(IGGAPICaller caller, Entity entity) throws GGAPIException;

	List<Entity> getEntities(IGGAPICaller caller, IGGAPIPageable pageable, IGGAPIFilter filter, IGGAPISort sort) throws GGAPIException;

	void save(IGGAPICaller caller, Entity entity) throws GGAPIException;

//	Entity update(IGGAPICaller caller, Entity entity) throws GGAPIException;

	Entity getOneById(IGGAPICaller caller, String id) throws GGAPIException;

	void delete(IGGAPICaller caller, Entity entity) throws GGAPIException;

	boolean doesExist(IGGAPICaller caller, String uuid) throws GGAPIException;
	
	Entity getOneByUuid(IGGAPICaller caller, String uuid) throws GGAPIException;

	long getCount(IGGAPICaller caller, IGGAPIFilter filter) throws GGAPIException;

	String getTenant(Entity entity) throws GGAPIException;

	void setDaos(List<Pair<Class<?>, IGGAPIDao<?>>> daos);

}
