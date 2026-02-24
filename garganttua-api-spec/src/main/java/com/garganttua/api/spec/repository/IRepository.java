package com.garganttua.api.spec.repository;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.api.spec.ApiException;

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

	Optional<Object> getOneById(String id) throws ApiException;

	void delete(Object entity) throws ApiException;

	boolean doesExist(String uuid) throws ApiException;

	Optional<Object> getOneByUuid(String uuid) throws ApiException;

	long getCount(IFilter filter) throws ApiException;

}
