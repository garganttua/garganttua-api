package com.garganttua.api.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.garganttua.api.business.IGGAPIBusiness;
import com.garganttua.api.connector.IGGAPIConnector;
import com.garganttua.api.controller.GGAPIEngineController;
import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.engine.registries.IGGAPIControllersRegistry;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.GGAPIObjectsHelper;
import com.garganttua.api.spec.IGGAPIEntity;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = "controllersRegistry")
public class GGAPIControllersRegistry implements IGGAPIControllersRegistry {
	
	@Autowired
	private GGAPIObjectsHelper helper;
	
	@Value("${com.garganttua.api.magicTenantId}")
	private String magicTenantId;
	
	private Map<String, IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> controllers = new HashMap<String, IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>();
	
	@Autowired
	private IGGAPIDynamicDomainsRegistry dynamicDomains;
	
	@Autowired
	private IGGAPIRepositoriesRegistry repositoriesRegistry;

	@Override
	public IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getController(String name) {
		return this.controllers.get(name);
	}
	
	@SuppressWarnings({ "unchecked"})
	@PostConstruct
	private void init() throws GGAPIEngineException {

		log.info("Creating Controllers ...");
		for( GGAPIDynamicDomain ddomain: this.dynamicDomains.getDynamicDomains() ){
			
			IGGAPIRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> repo  = (IGGAPIRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>) repositoriesRegistry.getRepository(ddomain.domain());
			
			String controller__ = ddomain.controller();
			
			IGGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> controller;
			if (controller__ != null && !controller__.isEmpty()) {
				controller = this.helper.getObjectFromConfiguration(controller__, IGGAPIController.class);
			} else {
				controller = new GGAPIEngineController();
			}
			
			controller.setDomain(ddomain.getDomain());
			
			Optional<IGGAPIRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>> repoObj = Optional.ofNullable(repo);
			Optional<IGGAPIBusiness<IGGAPIEntity>> businessObj = this.getBusiness(ddomain);
			Optional<IGGAPIConnector<IGGAPIEntity, List<IGGAPIEntity>, IGGAPIDTOObject<IGGAPIEntity>>> connectorObj = Optional.ofNullable(null);

			controller.setRepository(repoObj);
			controller.setConnector(connectorObj);
			controller.setBusiness(businessObj);
			controller.setTenant(ddomain.tenantEntity());
			controller.setOwnedEntity(ddomain.ownedEntity());
			controller.setUnicity(ddomain.unicity());
			
			this.controllers.put(ddomain.domain(), controller);
			
			log.info("	Controller added [domain {}]", ddomain.domain());
		}
	}

	@SuppressWarnings("unchecked")
	private Optional<IGGAPIBusiness<IGGAPIEntity>> getBusiness(GGAPIDynamicDomain ddomain) throws GGAPIEngineException {
		if( ddomain.business() != null && !ddomain.business().isEmpty() ) {
			return Optional.ofNullable(this.helper.getObjectFromConfiguration(ddomain.business(), IGGAPIBusiness.class));
		}
		return Optional.ofNullable(null);
	}

	@Override
	public List<IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> getControllers() {
		return new ArrayList<IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>(this.controllers.values());
	}

}
