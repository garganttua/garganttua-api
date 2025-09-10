package com.garganttua.api.core.engine;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.javatuples.Pair;

import com.garganttua.api.core.accessRules.AccessRulesFactory;
import com.garganttua.api.core.caller.CallerFactoriesFactory;
import com.garganttua.api.core.dao.DaosFactory;
import com.garganttua.api.core.domain.DomainsFactory;
import com.garganttua.api.core.factory.EntityFactoriesFactory;
import com.garganttua.api.core.interfasse.InterfacesFactory;
import com.garganttua.api.core.repository.RepositoriesFactory;
import com.garganttua.api.core.service.ServicesFactory;
import com.garganttua.api.core.service.ServicesInfosFactory;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.caller.ICallerFactoriesRegistry;
import com.garganttua.api.spec.caller.ICallerFactory;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.dao.IDaosRegistry;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.domain.IDomainsRegistry;
import com.garganttua.api.spec.engine.IAccessRulesRegistry;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.factory.IFactoriesRegistry;
import com.garganttua.api.spec.factory.IFactory;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.interfasse.IInterfacesRegistry;
import com.garganttua.api.spec.repository.IRepositoriesRegistry;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.service.IService;
import com.garganttua.api.spec.service.IServiceInfos;
import com.garganttua.api.spec.service.IServicesInfosRegistry;
import com.garganttua.api.spec.service.IServicesRegistry;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Engine implements IEngine {

	private IGGBeanLoader loader;
	private IDomainsRegistry domainRegistry;
	private List<String> packages;
	private IDaosRegistry daosRegistry;
	private IRepositoriesRegistry repositoriesRegistry;
	private IGGPropertyLoader propLoader;
	private IFactoriesRegistry factoriesRegistry;
	private IServicesRegistry servicesRegistry;
	private IInterfacesRegistry interfacesRegistry;
	private ICallerFactoriesRegistry callerFactoriesRegistry;
	private IAccessRulesRegistry accessRulesRegistry;
	private IServicesInfosRegistry servicesInfosRegistry;
	private Optional<IGGInjector> injector;

	protected Engine(Optional<IGGInjector> injector, List<String> packages, IGGPropertyLoader propLoader, IGGBeanLoader loader) {
		this.packages = packages;
		this.propLoader = propLoader;
		this.injector = injector;
		this.loader = loader;
	}

	@Override
	public IEngine start() throws CoreException {
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
		
		for( IInterface interfasse: this.interfacesRegistry.getInterfaces() ) {
			log.info("*** Starting interface "+interfasse.getName());
			interfasse.start();
		};
		
		return this;
	}

	@SuppressWarnings("unchecked")
	private void assemblyDomain(IDomain domain) {
		String domainName = domain.getDomain();
		log.info("*** Assembling domain "+domainName);
		
		List<Pair<Class<?>, IDao>> daos = this.daosRegistry.getDao(domainName);
		IRepository repository = (IRepository) this.repositoriesRegistry.getRepository(domainName);
		IFactory factory = (IFactory) this.factoriesRegistry.getFactory(domainName);
		IService service = this.servicesRegistry.getService(domainName);
		List<IInterface> interfaces = this.interfacesRegistry.getInterfaces(domainName);
		
		repository.setDaos(daos);
		factory.setRepository(repository);
		service.setFactory(factory);
		interfaces.forEach(interfasse -> {
			interfasse.setService(service);
		});
	}

	@Override
	public IEngine stop() throws CoreException {
		log.info("== STOPPING GARGANTTUA API ENGINE ==");
		return this;
	}

	@Override
	public IEngine reload() throws CoreException {
		log.info("== RELOADING GARGANTTUA API ENGINE ==");
		this.stop();
		this.flush();
		this.init();
		this.start();
		return this;
	}

	@Override
	public IEngine flush() throws CoreException {
		log.info("== FLUSHING GARGANTTUA API ENGINE ==");
		return this;
	}

	@Override
	public IEngine init() throws CoreException {
		log.info("============================================");
		log.info("======                                ======");
		log.info("======      Garganttua API Engine     ======");
		log.info("======                                ======");
		log.info("============================================");
		log.info("Version: {}", this.getClass().getPackage().getImplementationVersion());
		log.info("== INITIALIZING GARGANTTUA API ENGINE ==");
		
		this.domainRegistry = new DomainsFactory(this.packages).getRegistry();
		this.daosRegistry =  new DaosFactory(this.domainRegistry.getDomains(), this.loader).getRegistry();
		this.repositoriesRegistry = new RepositoriesFactory(this.domainRegistry.getDomains()).getRegistry();
		this.factoriesRegistry = new EntityFactoriesFactory(this.domainRegistry.getDomains(), this.injector).getRegistry();
		this.servicesRegistry = new ServicesFactory(this.domainRegistry.getDomains(), this.loader).getRegistry();
		this.interfacesRegistry = new InterfacesFactory(this.domainRegistry.getDomains(), this.loader).getRegistry();
		this.servicesInfosRegistry = new ServicesInfosFactory(this.domainRegistry.getDomains(), this.interfacesRegistry, this.factoriesRegistry).getRegistry();
		this.accessRulesRegistry = new AccessRulesFactory(this.domainRegistry.getDomains()).getRegistry();
		this.callerFactoriesRegistry = new CallerFactoriesFactory(this.domainRegistry.getDomains(), this.factoriesRegistry, this.accessRulesRegistry).getRegistry();

		return this;
	}

	@Override
	public List<String> getAuthorities() {
		return this.accessRulesRegistry.getAuthorities();
	}

	@Override
	public String getAuthority(EntityOperation method) {
		return this.accessRulesRegistry.getAuthority(method);
	}

	@Override
	public IDomain getTenantsDomain() {
		return this.domainRegistry.getTenantDomain();
	}

	@Override
	public Set<IDomain> getDomains() {
		return this.domainRegistry.getDomains();
	}

	@Override
	public void addServicesInfos(IDomain domain, List<IServiceInfos> authenticationServiceInfos) {
		this.servicesInfosRegistry.addServicesInfos(domain, authenticationServiceInfos);
	}

	@Override
	public void addAccessRule(IAccessRule accessRule) {
		this.accessRulesRegistry.addAccessRule(accessRule);
	}

	@Override
	public IService getService(String domain) {
		return this.servicesRegistry.getService(domain);
	}

	@Override
	public String getTenantDomainName() {
		return this.domainRegistry.getTenantDomain().getDomain();
	}

	@Override
	public List<IServiceInfos> getServicesInfos() {
		return this.servicesInfosRegistry.getServicesInfos();
	}

	@Override
	public ICaller getCaller(String domainName, EntityOperation operation, String path, String tenantId,
			String ownerId, String requestedtenantId, Object object) throws CoreException {
		ICallerFactory callerFactory = this.callerFactoriesRegistry.getCallerFactory(domainName);
		if( callerFactory == null ) {
			throw new EngineException(CoreExceptionCode.OBJECT_NOT_FOUND, "No caller factory for domain "+domainName);
		}
		return callerFactory.getCaller(operation, path, tenantId, ownerId, requestedtenantId, requestedtenantId);
	}

	@Override
	public Optional<IDomain> getDomain(String domain) {
		return Optional.ofNullable(this.domainRegistry.getDomain(domain));
	}

	@Override
	public List<IAccessRule> getAccessRules() {
		return this.accessRulesRegistry.getAccessRules();
	}

	@Override
	public IService getTenantService() {
		return this.servicesRegistry.getService(this.getTenantDomainName());
	}

	@Override
	public Collection<IService> getServices() {
		return this.servicesRegistry.getServices();
	}

	@Override
	public IFactory getFactory(String domainName) {
		return this.factoriesRegistry.getFactory(domainName);
	}

	@Override
	public IRepository getRepository(String domainName) {
		return this.repositoriesRegistry.getRepository(domainName);
	}
}
