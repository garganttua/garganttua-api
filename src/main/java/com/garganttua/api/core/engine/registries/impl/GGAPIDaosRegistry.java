package com.garganttua.api.core.engine.registries.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.dao.GGAPIDao;
import com.garganttua.api.core.dao.IGGAPIDAORepository;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.GGAPIDtoInfos;
import com.garganttua.api.core.engine.GGAPIDomain;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.engine.GGAPIObjectsHelper;
import com.garganttua.api.core.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIDomainsRegistry;
import com.garganttua.api.daos.mongodb.GGAPIMongoRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = "daosRegistry")
public class GGAPIDaosRegistry implements IGGAPIDaosRegistry {
	
	@Autowired
	private GGAPIObjectsHelper helper;
	
	@Autowired
	protected Optional<MongoTemplate> mongo;

	private Map<String, List<Pair<Class<?>,IGGAPIDAORepository<?>>>> daos = new HashMap<String, List<Pair<Class<?>,IGGAPIDAORepository<?>>>>();

	@Autowired
	private IGGAPIDomainsRegistry domains;

	@Value("${com.garganttua.api.superTenantId}")
	private String magicTenantId;
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	private void init() throws GGAPIEngineException {

		log.info("Creating DAOs ...");
		for( GGAPIDomain domain: this.domains.getDomains() ){
			List<Pair<Class<?>,IGGAPIDAORepository<?>>> daos = new ArrayList<Pair<Class<?>,IGGAPIDAORepository<?>>>();
			
			for( Pair<Class<?>, GGAPIDtoInfos> dto: domain.dtos) {
			
				String db = dto.getValue1().db();
					
				IGGAPIDAORepository<Object> dao = null;
				
				if( db != null && !db.isEmpty()) {
					switch(db) {
					case GGAPIDao.FS:
						dao = null;
						break;
					case GGAPIDao.MONGO:
						dao = new GGAPIMongoRepository(this.mongo.get());
						break;
					default:
						dao = this.helper.getObjectFromConfiguration(db, IGGAPIDAORepository.class);
						break;
					}
				}
				
				dao.setDomain(domain);
				dao.setDtoClass((Class<Object>) dto.getValue0());
				
				daos.add(new Pair<Class<?>, IGGAPIDAORepository<?>>(dto.getValue0(), dao));
				
				log.info("	DAO added [domain {}, dao {}]", domain.entity.getValue1().domain(), db);
			}
			this.daos.put(domain.entity.getValue1().domain(), daos);
		}
	}

	@Override
	public List<Pair<Class<?>, IGGAPIDAORepository<?>>> getDao(String domain) {
		return this.daos.get(domain);
	}

	@Override
	public List<Pair<Class<?>, IGGAPIDAORepository<?>>> getDaos() {
		List<Pair<Class<?>, IGGAPIDAORepository<?>>> daos = new ArrayList<Pair<Class<?>,IGGAPIDAORepository<?>>>();
		this.daos.forEach((k,v) -> {
			daos.addAll(v);
		});
		return daos;
	}

}
