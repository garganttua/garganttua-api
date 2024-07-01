package com.garganttua.api.core.filter;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.filter.IGGAPIFilter;

@FunctionalInterface
public interface IGGAPIFilterMapper {

	List<Pair<Class<?>, IGGAPIFilter>> map(IGGAPIDomain domain, IGGAPIFilter filter) throws GGAPIException;

}
