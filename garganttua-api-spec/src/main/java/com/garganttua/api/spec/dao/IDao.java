/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.spec.dao;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.IClass;

public interface IDao {

	void setDtoClass(IClass<?> dtoClass);
	
	List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort) throws ApiException;

	Object save(Object object) throws ApiException;

	void delete(Object object) throws ApiException;
	
	long count(IFilter filter) throws ApiException;
}
