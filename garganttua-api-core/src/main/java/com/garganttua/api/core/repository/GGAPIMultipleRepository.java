package com.garganttua.api.core.repository;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.dao.IGGAPIDao;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.sort.IGGAPISort;

public class GGAPIMultipleRepository implements IGGAPIRepository<Object> {

	@Override
	public void setEngine(IGGAPIEngine engine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean doesExist(IGGAPICaller caller, Object entity) throws GGAPIException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Object> getEntities(IGGAPICaller caller, IGGAPIPageable pageable, IGGAPIFilter filter,
			IGGAPISort sort) throws GGAPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(IGGAPICaller caller, Object entity) throws GGAPIException {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public Object update(IGGAPICaller caller, Object entity) throws GGAPIException {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Object getOneById(IGGAPICaller caller, String id) throws GGAPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(IGGAPICaller caller, Object entity) throws GGAPIException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean doesExist(IGGAPICaller caller, String uuid) throws GGAPIException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getOneByUuid(IGGAPICaller caller, String uuid) throws GGAPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getCount(IGGAPICaller caller, IGGAPIFilter filter) throws GGAPIException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTenant(Object entity) throws GGAPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDaos(List<Pair<Class<?>, IGGAPIDao<?>>> daos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
		// TODO Auto-generated method stub
		
	}

}
