package com.garganttua.api.core.filter.mapper;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.engine.GGAPIDomain;

public interface IGGAPIFilterMapper {

	List<Pair<Class<?>, GGAPILiteral>> map(GGAPIDomain domain, GGAPILiteral filter) throws GGAPILiteralMapperException;

}
