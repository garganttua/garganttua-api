package com.garganttua.api.engine.registries;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.repository.dao.IGGAPIDAORepository;

public interface IGGAPIDaosRegistry {

	List<Pair<Class<?>, IGGAPIDAORepository<?>>> getDao(String domain);

	List<Pair<Class<?>, IGGAPIDAORepository<?>>> getDaos();

}
