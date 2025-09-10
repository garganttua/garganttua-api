package com.garganttua.api.core.repository;

import java.util.List;
import java.util.Optional;

import org.javatuples.Pair;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.sort.ISort;

public class MultipleRepository implements IRepository {

	@Override
	public void setEngine(IEngine engine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean doesExist(ICaller caller, Object entity) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Object> getEntities(ICaller caller, IPageable pageable, IFilter filter,
			ISort sort) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(ICaller caller, Object entity) throws CoreException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Optional<Object> getOneById(ICaller caller, String id) throws CoreException {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public void delete(ICaller caller, Object entity) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean doesExist(ICaller caller, String uuid) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<Object> getOneByUuid(ICaller caller, String uuid) throws CoreException {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public long getCount(ICaller caller, IFilter filter) throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDaos(List<Pair<Class<?>, IDao>> daos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDomain(IDomain domain) {
		// TODO Auto-generated method stub
		
	}

}
