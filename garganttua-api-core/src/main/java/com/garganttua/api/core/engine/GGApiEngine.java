package com.garganttua.api.core.engine;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.core.dao.GGAPIDaosFactory;
import com.garganttua.api.core.domain.GGAPIDomainsFactory;
import com.garganttua.api.core.factory.GGAPIEntityFactoriesFactory;
import com.garganttua.api.core.repository.GGAPIRepositoriesFactory;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.dao.IGGAPIDaosRegistry;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;
import com.garganttua.api.spec.engine.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.engine.IGGAPIFactoriesRegistry;
import com.garganttua.api.spec.engine.IGGAPIServicesRegistry;
import com.garganttua.api.spec.repository.IGGAPIRepositoriesRegistry;
import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGApiEngine implements IGGAPIEngine {

	private IGGAPISecurity provider;
	private IGGBeanLoader loader;
	private IGGAPIDomainsRegistry domainRegistry;
	private List<String> packages;
	private IGGAPIDaosRegistry daosRegistry;
	private IGGAPIRepositoriesRegistry repositoriesRegistry;
	private IGGInjector injector;
	private IGGPropertyLoader propLoader;
	private IGGAPIFactoriesRegistry factoriesRegistry;

	protected GGApiEngine(IGGAPISecurity provider, IGGBeanLoader loader, List<String> packages, IGGInjector injector, IGGPropertyLoader propLoader) {
		this.provider = provider;
		this.loader = loader;
		this.packages = packages;
		this.injector = injector;
		this.propLoader = propLoader;
	}

	@Override
	public IGGAPIEngine start() throws GGAPIException {
		log.info("== START GGAPI ENGINE ==");
		return this;
	}

	@Override
	public IGGAPIEngine stop() throws GGAPIException {
		log.info("== STOP GGAPI ENGINE ==");
		return this;
	}

	@Override
	public IGGAPIEngine reload() throws GGAPIException {
		log.info("== RELOAD GGAPI ENGINE ==");
		this.stop();
		this.flush();
		this.init();
		this.start();
		return this;
	}

	@Override
	public IGGAPIEngine flush() throws GGAPIException {
		log.info("== FLUSH GGAPI ENGINE ==");
		return this;
	}

	@Override
	public IGGAPIEngine init() throws GGAPIException {
		log.info("============================================");
		log.info("======                                ======");
		log.info("======      Garganttua API Engine     ======");
		log.info("======                                ======");
		log.info("============================================");
		log.info("Version: {}", this.getClass().getPackage().getImplementationVersion());
		log.info("== INIT GGAPI ENGINE ==");
		
		this.domainRegistry = new GGAPIDomainsFactory(this.packages).getRegistry();
		this.daosRegistry =  new GGAPIDaosFactory(this.domainRegistry.getDomains(), loader).getRegistry();
		this.repositoriesRegistry = new GGAPIRepositoriesFactory(this.domainRegistry.getDomains()).getRegistry();
		this.factoriesRegistry = new GGAPIEntityFactoriesFactory(this.domainRegistry.getDomains()).getRegistry();
		
		
		return this;
	}

	@Override
	public IGGAPIDomainsRegistry getDomainsRegistry() {
		return this.domainRegistry;
	}

	@Override
	public IGGAPIDaosRegistry getDaosRegistry() {
		return this.daosRegistry;
	}

	@Override
	public IGGAPIRepositoriesRegistry getRepositoriesRegistry() {
		return this.repositoriesRegistry;
	}

	@Override
	public IGGAPIFactoriesRegistry getFactoriesRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGGAPIServicesRegistry getServicesRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGGAPIAccessRulesRegistry getAccessRulesRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGGAPIDomain getOwnerDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGGAPIDomain getTenantDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IGGAPISecurityEngine> getSecurity() {
		return Optional.empty();
	}
}
