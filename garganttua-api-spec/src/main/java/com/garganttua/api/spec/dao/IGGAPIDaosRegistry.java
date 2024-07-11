package com.garganttua.api.spec.dao;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.engine.IGGAPIEngineObject;

public interface IGGAPIDaosRegistry extends IGGAPIEngineObject {

	List<Pair<Class<?>, IGGAPIDao<?>>> getDao(String domain);

	List<Pair<Class<?>, IGGAPIDao<?>>> getDaos();

}
