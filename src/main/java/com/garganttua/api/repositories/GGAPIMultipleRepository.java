package com.garganttua.api.repositories;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.domain.GGAPIDomain;
import com.garganttua.api.core.repository.GGAPIRepositoryException;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.dao.GGAPILiteral;
import com.garganttua.api.spec.dao.GGAPISort;
import com.garganttua.api.spec.dao.IGGAPIDAORepository;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.repository.IGGAPIRepository;

public class GGAPIMultipleRepository implements IGGAPIRepository<Object> {

	@Override
	public void setEngine(IGGAPIEngine engine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDomain(GGAPIDomain domain) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean doesExist(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Object> getEntities(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter,
			GGAPISort sort) throws GGAPIRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object update(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getOneById(IGGAPICaller caller, String id) throws GGAPIRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean doesExist(IGGAPICaller caller, String uuid) throws GGAPIRepositoryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getOneByUuid(IGGAPICaller caller, String uuid) throws GGAPIRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getCount(IGGAPICaller caller, GGAPILiteral filter) throws GGAPIRepositoryException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTenant(Object entity) throws GGAPIRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDaos(List<Pair<Class<?>, IGGAPIDAORepository<?>>> daos) {
		// TODO Auto-generated method stub
		
	}

}
