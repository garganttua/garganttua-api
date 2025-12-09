package com.garganttua.api.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.dao.IDaosRegistry;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.dto.DtoInfos;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.beans.GGBeanRefValidator;
import com.garganttua.core.reflection.beans.IGGBeanLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DaosFactory {

	private Collection<IDomain> domains;
	private Map<String, List<Pair<Class<?>,IDao>>> daos = new HashMap<String, List<Pair<Class<?>,IDao>>>();
	private IGGBeanLoader beanLoader;

	public DaosFactory(Collection<IDomain> domains, IGGBeanLoader beanLoader) throws CoreException {
		this.domains = domains;
		this.beanLoader = beanLoader;
		try {
			this.collectDaos();
		} catch (ReflectionException e) {
			throw new EngineException(e);
		}
	}

	@SuppressWarnings({ "unchecked" })
	private void collectDaos() throws ReflectionException {
		log.info("*** Creating Daos ...");
		for( IDomain domain: this.domains ){
			ArrayList<Pair<Class<?>, IDao>> domainDaos = new ArrayList<Pair<Class<?>, IDao>>();
			List<Pair<Class<?>, DtoInfos>> domainDtos = domain.getDtos();
			
			for( Pair<Class<?>, DtoInfos> dto: domainDtos ) {
				String db = dto.getValue1().db();
				Pair<String, String> beanRef = GGBeanRefValidator.validate(db);
				
				IDao dao = (IDao) this.beanLoader.getBeanNamed(beanRef.getValue0(), beanRef.getValue1());
				dao.setDtoClass((Class<Object>) dto.getValue0());
				dao.setDomain(domain);
				
				domainDaos.add(new Pair<Class<?>, IDao>(dto.getValue0(), dao));
				
				log.info("	Dao added [domain {}, dao {}]", domain.getDomain(), db);
			}

			this.daos.put(domain.getDomain(), domainDaos);
		}
	}

	public IDaosRegistry getRegistry() {
		return new DaosRegistry(this.daos);
	}
}
