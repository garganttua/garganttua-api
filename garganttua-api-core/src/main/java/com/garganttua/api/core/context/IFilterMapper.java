package com.garganttua.api.core.context;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.ApiException;

@FunctionalInterface
public interface IFilterMapper {

	List<Pair<Class<?>, IFilter>> map(IDomainDefinition<?> domainDefinition, IFilter filter) throws ApiException;

}
