package com.garganttua.api.core.dao.fs;

import java.util.List;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

@GGBean(name = "FsDao", strategy = GGBeanLoadingStrategy.newInstance)
public class DaoFileSystem implements IDao {

	@Override
	public void setEngine(IEngine engine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDomain(IDomain domain) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDtoClass(Class<?> dtoClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Object> find(IPageable pageable, IFilter filter, ISort sort) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object save(Object object) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Object object) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long count(IFilter filter) throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

}
