package com.garganttua.api.core.filter.mapper;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.engine.GGAPIDomain;
import com.garganttua.api.core.filter.GGAPILiteral;

@FunctionalInterface
public interface IGGAPIFilterMapper {

	List<Pair<Class<?>, GGAPILiteral>> map(GGAPIDomain domain, GGAPILiteral filter) throws GGAPILiteralMapperException;

}
