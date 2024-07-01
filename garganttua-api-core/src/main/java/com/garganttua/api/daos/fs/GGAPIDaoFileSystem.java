package com.garganttua.api.daos.fs;

import java.util.List;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.dao.IGGAPIDao;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.sort.IGGAPISort;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

@GGBean(name = "FsDao", strategy = GGBeanLoadingStrategy.newInstance)
public class GGAPIDaoFileSystem implements IGGAPIDao<Object> {

	@Override
	public void setEngine(IGGAPIEngine engine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDtoClass(Class<Object> dtoClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Object> find(IGGAPIPageable pageable, IGGAPIFilter filter, IGGAPISort sort) throws GGAPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object save(Object object) throws GGAPIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Object object) throws GGAPIException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long count(IGGAPIFilter filter) throws GGAPIException {
		// TODO Auto-generated method stub
		return 0;
	}

}
