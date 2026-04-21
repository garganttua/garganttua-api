package com.garganttua.api.commons.repository;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.api.commons.ApiException;

/**
 *
 * @author JérémyCOLOMBET
 *
 */
public interface IRepository {

	boolean doesExist(Object entity) throws ApiException;

	List<Object> getEntities(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
			throws ApiException;

	void save(Object entity) throws ApiException;

	void delete(Object entity) throws ApiException;

	boolean doesExist(String uuid) throws ApiException;

	long getCount(IFilter filter) throws ApiException;

}
