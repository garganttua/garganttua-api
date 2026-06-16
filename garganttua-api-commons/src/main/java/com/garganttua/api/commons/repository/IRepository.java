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

	/**
	 * Get entities with an optional field projection (list of ENTITY field names) pushed down to the
	 * DAO for IO savings where supported. The default ignores the projection and delegates to the
	 * 3-arg variant, so callers that never project (unicity checks, lookups) are unaffected.
	 */
	default List<Object> getEntities(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort,
			Optional<List<String>> projection) throws ApiException {
		return getEntities(pageable, filter, sort);
	}

	void save(Object entity) throws ApiException;

	void delete(Object entity) throws ApiException;

	boolean doesExist(String uuid) throws ApiException;

	long getCount(IFilter filter) throws ApiException;

}
