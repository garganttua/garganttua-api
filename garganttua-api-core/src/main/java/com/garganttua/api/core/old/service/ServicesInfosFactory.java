package com.garganttua.api.core.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.factory.IFactoriesRegistry;
import com.garganttua.api.spec.factory.IFactory;
import com.garganttua.api.spec.interfasse.ICustomizableInterface;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.interfasse.IInterfacesRegistry;
import com.garganttua.api.spec.service.CustomService;
import com.garganttua.api.spec.service.IServiceInfos;
import com.garganttua.api.spec.service.IServicesInfosRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServicesInfosFactory {

	private Set<IDomain> domains;
	private IInterfacesRegistry interfacesRegistry;
	private Map<String, List<IServiceInfos>> servicesInfos = new HashMap<String, List<IServiceInfos>>();
	private IFactoriesRegistry factoriesRegistry;

	public ServicesInfosFactory(Set<IDomain> domains, IInterfacesRegistry interfacesRegistry,
			IFactoriesRegistry factoriesRegistry) {
		this.domains = domains;
		this.interfacesRegistry = interfacesRegistry;
		this.factoriesRegistry = factoriesRegistry;
		this.init();
	}

	private void init() {
		this.domains.parallelStream().forEach(domain -> {
			this.createInfos(domain);
		});
	}

	private void createInfos(IDomain domain) {
		List<IInterface> interfasses = this.interfacesRegistry.getInterfaces(domain.getDomain());

		List<IServiceInfos> customInfos = new ArrayList<IServiceInfos>();
		this.getCustomServiceFromClass(domain, domain.getEntityClass(), customInfos, () -> {
			try {
				Object newInstance = EntityHelper.newInstance(domain.getEntityClass());
				IFactory factory = this.factoriesRegistry.getFactory(domain.getDomain());
				newInstance = factory.prepareNewEntity(new HashMap<String, String>(), newInstance, null, null);
				
				return newInstance;
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		});
		domain.addServicesInfos(customInfos);
		customInfos.forEach(info -> {
			log.info("	Method added [domain {}, service {}]", domain.getDomain(), info);
		});

		interfasses.stream().filter(interfasse -> {
			return ICustomizableInterface.class.isAssignableFrom(interfasse.getClass());
		}).forEach(interfasse -> {
			List<IServiceInfos> infos;
			try {
				infos = ServicesInfosBuilder.buildServices(domain, interfasse);
			} catch (EngineException e) {
				throw new RuntimeException(e);
			}
			this.getCustomServicesFromObject(domain, interfasse, infos);
			customInfos.forEach( i -> {((ICustomizableInterface) interfasse).addCustomService(i);});
			
			infos.forEach(info -> {
				log.info("	Method added [domain {}, service {}]", domain.getDomain(), info);
			});

			domain.addServicesInfos(infos);
			customInfos.addAll(infos);
		});
		
		this.servicesInfos.put(domain.getDomain(), customInfos);
	}

	private void getCustomServicesFromObject(IDomain domain, Object customServiceProvider,
			List<IServiceInfos> infos) {
		this.getCustomServiceFromClass(domain, customServiceProvider.getClass(), infos, () -> {
			return customServiceProvider;
		});
	}

	private void getCustomServiceFromClass(IDomain domain, Class<?> customServiceProviderClass,
			List<IServiceInfos> infos, IObjectInstanciator instanciator) {
		Method[] methods = customServiceProviderClass.getDeclaredMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(CustomService.class)) {
				CustomService annotation = method.getAnnotation(CustomService.class);
				IServiceInfos service;
				try {
					service = ServicesInfosBuilder.getInfos(domain.getDomain(), customServiceProviderClass, method,
							annotation.path(), annotation.description(), EntityOperation.custom(domain.getDomain(),
									annotation.method(), annotation.entity(), annotation.actionOnAllEntities()),
							instanciator);
				} catch (EngineException e) {
					throw new RuntimeException(e);
				}
				infos.add(service);
			}
		}
	}

	public IServicesInfosRegistry getRegistry() {
		return new ServicesInfosRegistry(this.servicesInfos);
	}

}
