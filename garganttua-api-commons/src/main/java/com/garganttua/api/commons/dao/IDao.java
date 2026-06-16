/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.commons.dao;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;

public interface IDao {

	void registerDomain(IDomainDefinition<?> domainDefinition);

	List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort) throws ApiException;

	/**
	 * Find with an optional field projection — the list of ENTITY field names to return. A DAO that
	 * supports it (e.g. MongoDB) narrows the fetched fields for IO savings; the default ignores the
	 * projection and delegates to the 3-arg {@link #find(Optional, Optional, Optional)}, so existing
	 * implementations keep working unchanged. The projection is best-effort and never widens results.
	 */
	default List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort,
			Optional<List<String>> projection) throws ApiException {
		return find(pageable, filter, sort);
	}

	Object save(Object object) throws ApiException;

	void delete(Object object) throws ApiException;
	
	long count(IFilter filter) throws ApiException;
}
