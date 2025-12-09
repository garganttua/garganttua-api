package com.garganttua.api.spec.dao;

import java.util.List;

import org.javatuples.Pair;

public interface IDaosRegistry {

	List<Pair<Class<?>, IDao>> getDao(String domain);

	List<Pair<Class<?>, IDao>> getDaos();

}
