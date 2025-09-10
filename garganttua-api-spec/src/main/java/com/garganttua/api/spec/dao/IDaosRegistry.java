package com.garganttua.api.spec.dao;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.engine.IEngineObject;

public interface IDaosRegistry extends IEngineObject {

	List<Pair<Class<?>, IDao>> getDao(String domain);

	List<Pair<Class<?>, IDao>> getDaos();

}
