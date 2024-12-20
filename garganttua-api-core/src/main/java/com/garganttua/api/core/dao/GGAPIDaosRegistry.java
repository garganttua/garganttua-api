package com.garganttua.api.core.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.dao.IGGAPIDao;
import com.garganttua.api.spec.dao.IGGAPIDaosRegistry;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;

public class GGAPIDaosRegistry implements IGGAPIDaosRegistry {

	private Map<String, List<Pair<Class<?>, IGGAPIDao<?>>>> daos;
	
	public GGAPIDaosRegistry(Map<String, List<Pair<Class<?>, IGGAPIDao<?>>>> daos) {
		this.daos = daos;
	}

	@Override
	public List<Pair<Class<?>, IGGAPIDao<?>>> getDao(String domain) {
		return this.daos.get(domain);
	}

	@Override
	public List<Pair<Class<?>, IGGAPIDao<?>>> getDaos() {
		List<Pair<Class<?>, IGGAPIDao<?>>> daos = new ArrayList<Pair<Class<?>,IGGAPIDao<?>>>();
		this.daos.forEach((k,v) -> {
			daos.addAll(v);
		});
		return daos;
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {
		this.daos.values().parallelStream().forEach(list -> {
			list.parallelStream().forEach(pair -> {
				pair.getValue1().setEngine(engine);
			});
		});
	}
}
