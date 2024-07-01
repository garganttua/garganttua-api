package com.garganttua.api.core.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.dao.IGGAPIDao;
import com.garganttua.api.spec.dao.IGGAPIDaosRegistry;

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
}
