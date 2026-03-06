package com.garganttua.api.spec.dao;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.core.reflection.IClass;

public interface IDaosRegistry {

	List<Pair<IClass<?>, IDao>> getDao(String domain);

	List<Pair<IClass<?>, IDao>> getDaos();

}
