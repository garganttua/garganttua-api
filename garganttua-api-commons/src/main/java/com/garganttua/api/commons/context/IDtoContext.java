package com.garganttua.api.commons.context;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.api.commons.ApiException;

public interface IDtoContext<D> {

    IDtoDefinition<D> getDtoDefinition();

    IDao getDao() throws ApiException;

    String getUuid(Object object) throws ApiException;

	default List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort) throws ApiException {
        return getDao().find(pageable, filter, sort);
    }

	/** Find with an optional field projection, delegated to the DAO (best-effort; default ignores it). */
	default List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort,
			Optional<List<String>> projection) throws ApiException {
        return getDao().find(pageable, filter, sort, projection);
    }

	default Object save(Object object) throws ApiException {
        return getDao().save(object);
    }

	default void delete(Object object) throws ApiException {
        getDao().delete(object);
    }
	
	default long count(IFilter filter) throws ApiException {
        return getDao().count(filter);
    }

}
