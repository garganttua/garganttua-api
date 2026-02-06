package com.garganttua.api.spec.context;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.definition.IDtoDefinition;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.CoreException;

public interface IDtoContext<D> {

    IDtoDefinition<D> getDtoDefinition();

    IDao getDao() throws CoreException;

    String getUuid(Object object) throws CoreException;

	default List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort) throws CoreException {
        return getDao().find(pageable, filter, sort);
    }

	default Object save(Object object) throws CoreException {
        return getDao().save(object);
    }

	default void delete(Object object) throws CoreException {
        getDao().delete(object);
    }
	
	default long count(IFilter filter) throws CoreException {
        return getDao().count(filter);
    }

}
