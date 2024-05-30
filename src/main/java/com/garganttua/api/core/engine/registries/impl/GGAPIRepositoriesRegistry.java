package com.garganttua.api.core.engine.registries.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.engine.GGAPIDomain;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.engine.GGAPIObjectsHelper;
import com.garganttua.api.repositories.GGAPIMultipleRepository;
import com.garganttua.api.repositories.GGAPISimpleRepository;
import com.garganttua.api.spec.dao.IGGAPIDAORepository;
import com.garganttua.api.spec.engine.IGGAPIDaosRegistry;
import com.garganttua.api.spec.engine.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.engine.IGGAPIRepositoriesRegistry;
import com.garganttua.api.spec.repository.IGGAPIRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = "repositoriesRegistry")
public class GGAPIRepositoriesRegistry implements IGGAPIRepositoriesRegistry {
	
	@Autowired
	private GGAPIObjectsHelper helper;
	
	@Value("${com.garganttua.api.superTenantId}")
	private String magicTenantId;
	
	private Map<String, IGGAPIRepository<?>> repositories = new HashMap<String, IGGAPIRepository<?>>();
	
	@Autowired
	private IGGAPIDomainsRegistry domains;
	
	@Autowired
	private IGGAPIDaosRegistry daosRegistry;

	@Override
	public IGGAPIRepository<?> getRepository(String name) {
		return this.repositories.get(name);
	}

	@PostConstruct
	private void init() throws GGAPIEngineException {

		log.info("Creating Repositories ...");
		for( GGAPIDomain ddomain: this.domains.getDomains() ){
			
			List<Pair<Class<?>, IGGAPIDAORepository<?>>> dao = daosRegistry.getDao(ddomain.entity.getValue1().domain());
			
			if( dao == null ) {
				throw new GGAPIEngineException("DAO "+ddomain.entity.getValue1().domain()+" not found");
			}
			
			String repo__ = ddomain.repo;
			
			IGGAPIRepository<?> repo = null;
			
			if (repo__ != null && !repo__.isEmpty()) {
				repo = helper.getObjectFromConfiguration(repo__, IGGAPIRepository.class);
			} else {
				if(ddomain.dtos.size() == 1) {
					repo = new GGAPISimpleRepository();
				} else {
					repo = new GGAPIMultipleRepository();
				}
			}

			repo.setDaos(dao);
			repo.setDomain(ddomain);
			
			this.repositories.put(ddomain.entity.getValue1().domain(), repo);

			log.info("	Repository added [domain {}, repo {}]", ddomain.entity.getValue1().domain(), repo);
		}
	}

	@Override
	public List<IGGAPIRepository<?>> getRepositories() {
		return new ArrayList<IGGAPIRepository<?>>(this.repositories.values());
	}
}
