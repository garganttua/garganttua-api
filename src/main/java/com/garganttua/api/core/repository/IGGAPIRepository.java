/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.repository;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.dao.IGGAPIDAORepository;
import com.garganttua.api.core.engine.IGGAPIEngineObject;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;

/**
 * 
 * @author JérémyCOLOMBET
 *
 * @param <Entity>
 */
public interface IGGAPIRepository<Entity> extends IGGAPIEngineObject {

	boolean doesExist(IGGAPICaller caller, Entity entity) throws GGAPIRepositoryException;

	List<Entity> getEntities(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort) throws GGAPIRepositoryException;

	void save(IGGAPICaller caller, Entity entity) throws GGAPIRepositoryException;

	Entity update(IGGAPICaller caller, Entity entity) throws GGAPIRepositoryException;

	Entity getOneById(IGGAPICaller caller, String id) throws GGAPIRepositoryException;

	void delete(IGGAPICaller caller, Entity entity) throws GGAPIRepositoryException;

	boolean doesExist(IGGAPICaller caller, String uuid) throws GGAPIRepositoryException;
	
	Entity getOneByUuid(IGGAPICaller caller, String uuid) throws GGAPIRepositoryException;

	long getCount(IGGAPICaller caller, GGAPILiteral filter) throws GGAPIRepositoryException;

	String getTenant(Entity entity) throws GGAPIRepositoryException;

	void setDaos(List<Pair<Class<?>, IGGAPIDAORepository<?>>> daos);

	
	
}
