package com.garganttua.api.core.legacy.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.dao.IDaosRegistry;
import com.garganttua.api.spec.domain.IDomain;

public class DaosRegistry implements IDaosRegistry {

	private Map<String, List<Pair<Class<?>, IDao>>> daos;
	
	public DaosRegistry(Map<String, List<Pair<Class<?>, IDao>>> daos) {
		this.daos = daos;
	}

	@Override
	public List<Pair<Class<?>, IDao>> getDao(String domain) {
		return this.daos.get(domain);
	}

	@Override
	public List<Pair<Class<?>, IDao>> getDaos() {
		List<Pair<Class<?>, IDao>> daos = new ArrayList<Pair<Class<?>,IDao>>();
		this.daos.forEach((k,v) -> {
			daos.addAll(v);
		});
		return daos;
	}

	@Override
	public void setDomain(IDomain domain) {
	}

	@Override
	public void setEngine(IEngine engine) {
		this.daos.values().parallelStream().forEach(list -> {
			list.parallelStream().forEach(pair -> {
				pair.getValue1().setEngine(engine);
			});
		});
	}
}
