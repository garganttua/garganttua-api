package com.garganttua.api.spec.engine;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.dao.IGGAPIDao;

public interface IGGAPIDaosRegistry {

	List<Pair<Class<?>, IGGAPIDao<?>>> getDao(String domain);

	List<Pair<Class<?>, IGGAPIDao<?>>> getDaos();

}
