package com.garganttua.api.core;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

@GGBean(strategy = GGBeanLoadingStrategy.newInstance)
public class DummyDao implements IDao {

    private List<Object> nextFind;

    public void setNextFindReturn(List<Object> objects){
        this.nextFind = objects;
    }

    @Override
    public void setEngine(IEngine engine) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setEngine'");
    }

    @Override
    public void setDomain(IDomain domain) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDomain'");
    }

    @Override
    public void setDtoClass(Class<?> dtoClass) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDtoClass'");
    }

    @Override
    public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort) throws CoreException {

        return this.nextFind;
    }

    @Override
    public Object save(Object object) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void delete(Object object) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public long count(IFilter filter) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }

}
