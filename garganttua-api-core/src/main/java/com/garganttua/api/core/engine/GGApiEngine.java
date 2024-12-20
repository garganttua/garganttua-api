package com.garganttua.api.core.engine;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.javatuples.Pair;

import com.garganttua.api.core.accessRules.GGAPIAccessRulesFactory;
import com.garganttua.api.core.caller.GGAPICallerFactoriesFactory;
import com.garganttua.api.core.dao.GGAPIDaosFactory;
import com.garganttua.api.core.domain.GGAPIDomainsFactory;
import com.garganttua.api.core.factory.GGAPIEntityFactoriesFactory;
import com.garganttua.api.core.interfasse.GGAPIInterfacesFactory;
import com.garganttua.api.core.repository.GGAPIRepositoriesFactory;
import com.garganttua.api.core.service.GGAPIServicesFactory;
import com.garganttua.api.core.service.GGAPIServicesInfosFactory;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.caller.IGGAPICallerFactoriesRegistry;
import com.garganttua.api.spec.caller.IGGAPICallerFactory;
import com.garganttua.api.spec.dao.IGGAPIDao;
import com.garganttua.api.spec.dao.IGGAPIDaosRegistry;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.factory.IGGAPIFactoriesRegistry;
import com.garganttua.api.spec.interfasse.IGGAPIInterface;
import com.garganttua.api.spec.interfasse.IGGAPIInterfacesRegistry;
import com.garganttua.api.spec.repository.IGGAPIRepositoriesRegistry;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.api.spec.service.IGGAPIServicesInfosRegistry;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGApiEngine implements IGGAPIEngine {

	private IGGBeanLoader loader;
	private IGGAPIDomainsRegistry domainRegistry;
	private List<String> packages;
	private IGGAPIDaosRegistry daosRegistry;
	private IGGAPIRepositoriesRegistry repositoriesRegistry;
	private IGGPropertyLoader propLoader;
	private IGGAPIFactoriesRegistry factoriesRegistry;
	private IGGAPIServicesRegistry servicesRegistry;
	private IGGAPIInterfacesRegistry interfacesRegistry;
	private IGGAPICallerFactoriesRegistry callerFactoriesRegistry;
	private IGGAPIAccessRulesRegistry accessRulesRegistry;
	private IGGAPIServicesInfosRegistry servicesInfosRegistry;
	private Optional<IGGInjector> injector;

	protected GGApiEngine(Optional<IGGInjector> injector, List<String> packages, IGGPropertyLoader propLoader, IGGBeanLoader loader) {
		this.packages = packages;
		this.propLoader = propLoader;
		this.injector = injector;
		this.loader = loader;
	}

	@Override
	public IGGAPIEngine start() throws GGAPIException {
		log.info("== STARTING GARGANTTUA API ENGINE ==");
		
		log.info("Assembling domains");
		
		this.domainRegistry.getDomains().forEach(domain -> {
			this.assemblyDomain(domain);
		});
		
		log.info("Injecting engine");
		this.daosRegistry.setEngine(this);
		this.domainRegistry.setEngine(this);
		this.factoriesRegistry.setEngine(this);
		this.interfacesRegistry.setEngine(this);
		this.repositoriesRegistry.setEngine(this);
		this.servicesRegistry.setEngine(this);
		
		log.info("Starting interfaces");
		
		for( IGGAPIInterface interfasse: this.interfacesRegistry.getInterfaces() ) {
			log.info("*** Starting interface "+interfasse.getName());
			interfasse.start();
		};
		
		return this;
	}

	@SuppressWarnings("unchecked")
	private void assemblyDomain(IGGAPIDomain domain) {
		String domainName = domain.getDomain();
		log.info("*** Assembling domain "+domainName);
		
		List<Pair<Class<?>, IGGAPIDao<?>>> daos = this.daosRegistry.getDao(domainName);
		IGGAPIRepository repository = (IGGAPIRepository) this.repositoriesRegistry.getRepository(domainName);
		IGGAPIEntityFactory<Object> factory = (IGGAPIEntityFactory<Object>) this.factoriesRegistry.getFactory(domainName);
		IGGAPIService service = this.servicesRegistry.getService(domainName);
		List<IGGAPIInterface> interfaces = this.interfacesRegistry.getInterfaces(domainName);
		
		repository.setDaos(daos);
		factory.setRepository(repository);
		service.setFactory(factory);
		interfaces.forEach(interfasse -> {
			interfasse.setService(service);
		});
	}

	@Override
	public IGGAPIEngine stop() throws GGAPIException {
		log.info("== STOPPING GARGANTTUA API ENGINE ==");
		return this;
	}

	@Override
	public IGGAPIEngine reload() throws GGAPIException {
		log.info("== RELOADING GARGANTTUA API ENGINE ==");
		this.stop();
		this.flush();
		this.init();
		this.start();
		return this;
	}

	@Override
	public IGGAPIEngine flush() throws GGAPIException {
		log.info("== FLUSHING GARGANTTUA API ENGINE ==");
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
		log.info("== INITIALIZING GARGANTTUA API ENGINE ==");
		
		this.domainRegistry = new GGAPIDomainsFactory(this.packages).getRegistry(); 
		this.daosRegistry =  new GGAPIDaosFactory(this.domainRegistry.getDomains(), this.loader).getRegistry();
		this.repositoriesRegistry = new GGAPIRepositoriesFactory(this.domainRegistry.getDomains()).getRegistry();
		this.factoriesRegistry = new GGAPIEntityFactoriesFactory(this.domainRegistry.getDomains(), this.injector).getRegistry();
		this.servicesRegistry = new GGAPIServicesFactory(this.domainRegistry.getDomains()).getRegistry();
		this.interfacesRegistry = new GGAPIInterfacesFactory(this.domainRegistry.getDomains(), this.loader).getRegistry();
		this.servicesInfosRegistry = new GGAPIServicesInfosFactory(this.domainRegistry.getDomains(), this.interfacesRegistry).getRegistry();
		this.accessRulesRegistry = new GGAPIAccessRulesFactory(this.domainRegistry.getDomains()).getRegistry();
		this.callerFactoriesRegistry = new GGAPICallerFactoriesFactory(this.domainRegistry.getDomains(), this.factoriesRegistry, this.accessRulesRegistry).getRegistry();

		return this;
	}

	@Override
	public List<String> getAuthorities() {
		return this.accessRulesRegistry.getAuthorities();
	}

	@Override
	public String getAuthority(GGAPIEntityOperation method) {
		return this.accessRulesRegistry.getAuthority(method);
	}

	@Override
	public IGGAPIDomain getTenantsDomain() {
		return this.domainRegistry.getTenantDomain();
	}

	@Override
	public Set<IGGAPIDomain> getDomains() {
		return this.domainRegistry.getDomains();
	}

	@Override
	public void addServicesInfos(IGGAPIDomain domain, List<IGGAPIServiceInfos> authenticationServiceInfos) {
		this.servicesInfosRegistry.addServicesInfos(domain, authenticationServiceInfos);
	}

	@Override
	public void addAccessRule(IGGAPIAccessRule accessRule) {
		this.accessRulesRegistry.addAccessRule(accessRule);
	}

	@Override
	public IGGAPIService getService(String domain) {
		return this.servicesRegistry.getService(domain);
	}

	@Override
	public String getTenantDomainName() {
		return this.domainRegistry.getTenantDomain().getDomain();
	}

	@Override
	public List<IGGAPIServiceInfos> getServicesInfos() {
		return this.servicesInfosRegistry.getServicesInfos();
	}

	@Override
	public IGGAPICaller getCaller(String domainName, GGAPIEntityOperation operation, String path, String tenantId,
			String ownerId, String requestedtenantId, Object object) throws GGAPIException {
		IGGAPICallerFactory callerFactory = this.callerFactoriesRegistry.getCallerFactory(domainName);
		if( callerFactory == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.OBJECT_NOT_FOUND, "No caller factory for domain "+domainName);
		}
		return callerFactory.getCaller(operation, path, tenantId, ownerId, requestedtenantId, requestedtenantId);
	}

	@Override
	public Optional<IGGAPIDomain> getDomain(String domain) {
		return Optional.ofNullable(this.domainRegistry.getDomain(domain));
	}

	@Override
	public List<IGGAPIAccessRule> getAccessRules() {
		return this.accessRulesRegistry.getAccessRules();
	}

	@Override
	public IGGAPIService getTenantService() {
		return this.servicesRegistry.getService(this.getTenantDomainName());
	}

	@Override
	public Collection<IGGAPIService> getServices() {
		return this.servicesRegistry.getServices();
	}

	@Override
	public IGGAPIEntityFactory<?> getFactory(String domainName) {
		return this.factoriesRegistry.getFactory(domainName);
	}
}
