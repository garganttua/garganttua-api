package com.garganttua.api.spec.filter;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.IClass;

@FunctionalInterface
public interface IFilterMapper {

	List<Pair<IClass<?>, IFilter>> map(IDomainDefinition<?> domainDefinition, IFilter filter) throws ApiException;

}
