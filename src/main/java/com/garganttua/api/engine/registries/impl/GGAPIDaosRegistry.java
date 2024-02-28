package com.garganttua.api.engine.registries.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.GGAPIObjectsHelper;
import com.garganttua.api.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.engine.registries.IGGAPIDynamicDomainsRegistry;
import com.garganttua.api.repository.dao.GGAPIDao;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dao.mongodb.GGAPIEngineMongoRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = "daosRegistry")
public class GGAPIDaosRegistry implements IGGAPIDaosRegistry {
	
	@Autowired
	private GGAPIObjectsHelper helper;
	
	@Autowired
	protected Optional<MongoTemplate> mongo;

	private Map<String, IGGAPIDAORepository> daos = new HashMap<String, IGGAPIDAORepository>();

	@Autowired
	private IGGAPIDynamicDomainsRegistry dynamicDomains;

	@Value("${com.garganttua.api.superTenantId}")
	private String magicTenantId;
	
	@PostConstruct
	private void init() throws GGAPIEngineException {

		log.info("Creating DAOs ...");
		for( GGAPIDynamicDomain ddomain: this.dynamicDomains.getDynamicDomains() ){
			GGAPIDao db = ddomain.db;
			String dao__ = ddomain.dao;
				
			IGGAPIDAORepository dao = null;
			
			if( dao__ != null && !dao__.isEmpty()) {
				dao = helper.getObjectFromConfiguration(ddomain.dao, IGGAPIDAORepository.class);
			} else {
				switch (db) {
				default:
				case mongo:
					if( this.mongo.isEmpty() ) {
						throw new GGAPIEngineException("No mongo connection available.");
					}
					dao = new GGAPIEngineMongoRepository();
					((GGAPIEngineMongoRepository) dao).setMongoTemplate(this.mongo.get());
					break;
				case fs:
					
					break;
				}
			}
			
			this.daos.put(ddomain.domain, dao);
			if( dao__ != null && !dao__.isEmpty() ) {
				log.info("	DAO added [domain {}, dao {}]", ddomain.domain, dao__);
			} else {
				log.info("	DAO added [domain {}, dao {}]", ddomain.domain, db);
			}
			
		}
	}

	@Override
	public IGGAPIDAORepository getDao (
			String name) {
		return this.daos.get(name);
	}

	@Override
	public List<IGGAPIDAORepository> getDaos() {
		return new ArrayList<IGGAPIDAORepository>(this.daos.values());
	}

}
