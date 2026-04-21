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

	Object save(Object object) throws ApiException;

	void delete(Object object) throws ApiException;
	
	long count(IFilter filter) throws ApiException;
}
