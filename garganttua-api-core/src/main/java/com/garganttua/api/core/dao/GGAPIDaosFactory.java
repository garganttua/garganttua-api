package com.garganttua.api.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.dao.IGGAPIDao;
import com.garganttua.api.spec.dao.IGGAPIDaosRegistry;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.beans.GGBeanRefValidator;
import com.garganttua.reflection.beans.IGGBeanLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIDaosFactory {

	private Collection<IGGAPIDomain> domains;
	private Map<String, List<Pair<Class<?>,IGGAPIDao<?>>>> daos = new HashMap<String, List<Pair<Class<?>,IGGAPIDao<?>>>>();
	private IGGBeanLoader beanLoader;

	public GGAPIDaosFactory(Collection<IGGAPIDomain> domains, IGGBeanLoader beanLoader) throws GGAPIException {
		this.domains = domains;
		this.beanLoader = beanLoader;
		try {
			this.collectDaos();
		} catch (GGReflectionException e) {
			throw new GGAPIEngineException(e);
		}
	}

	@SuppressWarnings({ "unchecked" })
	private void collectDaos() throws GGReflectionException {
		log.info("*** Creating Daos ...");
		for( IGGAPIDomain domain: this.domains ){
			ArrayList<Pair<Class<?>, IGGAPIDao<?>>> domainDaos = new ArrayList<Pair<Class<?>, IGGAPIDao<?>>>();
			List<Pair<Class<?>, GGAPIDtoInfos>> domainDtos = domain.getDtos();
			
			for( Pair<Class<?>, GGAPIDtoInfos> dto: domainDtos ) {
				String db = dto.getValue1().db();
				Pair<String, String> beanRef = GGBeanRefValidator.validate(db);
				
				IGGAPIDao<Object> dao = (IGGAPIDao<Object>) this.beanLoader.getBeanNamed(beanRef.getValue0(), beanRef.getValue1());
				dao.setDomain(domain);
				dao.setDtoClass((Class<Object>) dto.getValue0());
				
				domainDaos.add(new Pair<Class<?>, IGGAPIDao<?>>(dto.getValue0(), dao));
				
				log.info("	Dao added [domain {}, dao {}]", domain.getEntity().getValue1().domain(), db);
			}

			this.daos.put(domain.getEntity().getValue1().domain(), domainDaos);
		}
	}

	public IGGAPIDaosRegistry getRegistry() {
		return new GGAPIDaosRegistry(this.daos);
	}
}
