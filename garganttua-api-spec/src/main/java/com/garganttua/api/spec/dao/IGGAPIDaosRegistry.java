package com.garganttua.api.spec.dao;

import java.util.List;

import org.javatuples.Pair;

public interface IGGAPIDaosRegistry {

	List<Pair<Class<?>, IGGAPIDao<?>>> getDao(String domain);

	List<Pair<Class<?>, IGGAPIDao<?>>> getDaos();

}
