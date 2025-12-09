package com.garganttua.api.core.runtime.filter;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.core.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.filter.IFilter;

@FunctionalInterface
public interface IFilterMapper {

	List<Pair<Class<?>, IFilter>> map(IDomain domain, IFilter filter) throws CoreException;

}
