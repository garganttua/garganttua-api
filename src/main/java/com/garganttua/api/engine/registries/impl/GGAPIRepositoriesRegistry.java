package com.garganttua.api.engine.registries.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.GGAPIObjectsHelper;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.engine.registries.IGGAPIDynamicDomainsRegistry;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.repository.GGAPIEngineRepository;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = "repositoriesRegistry")
public class GGAPIRepositoriesRegistry implements IGGAPIRepositoriesRegistry {
	
	@Autowired
	private GGAPIObjectsHelper helper;
	
	@Value("${com.garganttua.api.superTenantId}")
	private String magicTenantId;
	
	private Map<String, IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> repositories = new HashMap<String, IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>();
	
	@Autowired
	private IGGAPIDynamicDomainsRegistry dynamicDomains;
	
	@Autowired
	private IGGAPIDaosRegistry daosRegistry;

	@Override
	public IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getRepository(String name) {
		return this.repositories.get(name);
	}

	@SuppressWarnings("unchecked")
	@PostConstruct
	private void init() throws GGAPIEngineException {

		log.info("Creating Repositories ...");
		for( GGAPIDynamicDomain ddomain: this.dynamicDomains.getDynamicDomains() ){
			
			IGGAPIDAORepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> dao = (IGGAPIDAORepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>) daosRegistry.getDao(ddomain.domain());
			
			if( dao == null ) {
				throw new GGAPIEngineException("DAO "+ddomain.domain()+" not found");
			}
			
			String repo__ = ddomain.repo();
			
			IGGAPIRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> repo = null;
			
			if (repo__ != null && !repo__.isEmpty()) {
				repo = helper.getObjectFromConfiguration(repo__, IGGAPIRepository.class);
			} else {
				repo = new GGAPIEngineRepository();
			}
			repo.setDomain(ddomain);
			repo.setDao((IGGAPIDAORepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>) dao);
			
			this.repositories.put(ddomain.domain(), repo);

			log.info("	Repository added [domain {}]", ddomain.domain());
			
		}
	}

	@Override
	public List<IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> getRepositories() {
		return new ArrayList<IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>(this.repositories.values());
	}
}
