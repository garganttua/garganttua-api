package com.garganttua.api.core.engine;

import java.util.Optional;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;
import com.garganttua.api.spec.engine.IGGAPIDaosRegistry;
import com.garganttua.api.spec.engine.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.engine.IGGAPIFactoriesRegistry;
import com.garganttua.api.spec.engine.IGGAPIRepositoriesRegistry;
import com.garganttua.api.spec.engine.IGGAPIServicesRegistry;
import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.reflection.beans.IGGBeanLoader;

//@Slf4j
public class GGApiEngine implements IGGAPIEngine {

	private IGGAPISecurity provider;
	private IGGBeanLoader loader;

	protected GGApiEngine(IGGAPISecurity provider, IGGBeanLoader loader) {
		this.provider = provider;
		this.loader = loader;
	}

	@Override
	public IGGAPIEngine start() throws GGAPIException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IGGAPIEngine stop() throws GGAPIException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IGGAPIEngine reload() throws GGAPIException {
		this.stop();
		this.flush();
		this.init();
		this.start();
		return this;
	}

	@Override
	public IGGAPIEngine flush() throws GGAPIException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IGGAPIEngine init() throws GGAPIException {
//		log.info("============================================");
//		log.info("======                                ======");
//		log.info("====== Starting Garganttua API Engine ======");
//		log.info("======                                ======");
//		log.info("============================================");
//		log.info("Version: {}", this.getClass().getPackage().getImplementationVersion());
//		log.info("== BOOTING ENGINE ==");
		
		
		
		return this;
	}

	@Override
	public IGGAPIDomainsRegistry getDomainsRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGGAPIDaosRegistry getDaosRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGGAPIRepositoriesRegistry getRepositoriesRegistry() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return Optional.empty();
	}


}
