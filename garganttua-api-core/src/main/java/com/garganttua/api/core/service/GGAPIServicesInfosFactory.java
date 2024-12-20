package com.garganttua.api.core.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.interfasse.IGGAPIInterface;
import com.garganttua.api.spec.interfasse.IGGAPIInterfacesRegistry;
import com.garganttua.api.spec.service.GGAPICustomService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.api.spec.service.IGGAPIServicesInfosRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIServicesInfosFactory {

	private Set<IGGAPIDomain> domains;
	private IGGAPIInterfacesRegistry interfacesRegistry;
	private Map<String, List<IGGAPIServiceInfos>> servicesInfos = new HashMap<String, List<IGGAPIServiceInfos>>();

	public GGAPIServicesInfosFactory(Set<IGGAPIDomain> domains, IGGAPIInterfacesRegistry interfacesRegistry) {
		this.domains = domains;
		this.interfacesRegistry = interfacesRegistry;
		this.init();
	}

	private void init() {
		this.domains.parallelStream().forEach(domain -> {
			this.createInfos(domain);
		});
	}

	private void createInfos(IGGAPIDomain domain) {
		List<IGGAPIInterface> interfasses = this.interfacesRegistry.getInterfaces(domain.getDomain());

		List<IGGAPIServiceInfos> customInfos = new ArrayList<IGGAPIServiceInfos>();
		this.getCustomServiceFromClass(domain, domain.getEntityClass(), customInfos, () -> {
			try {
				return GGAPIEntityHelper.newInstance(domain.getEntityClass());
			} catch (GGAPIException e) {
				throw new RuntimeException(e);
			}
		});
		domain.addServicesInfos(customInfos);
		customInfos.forEach(info -> {
			log.info("	Method added [domain {}, service {}]", domain.getDomain(), info);
		});

		interfasses.stream().forEach(interfasse -> {
			List<IGGAPIServiceInfos> infos;
			try {
				infos = GGAPIServicesInfosBuilder.buildGGAPIServices(domain, interfasse);
			} catch (GGAPIEngineException e) {
				throw new RuntimeException(e);
			}
			this.getCustomServicesFromObject(domain, interfasse, infos);
			customInfos.forEach( i -> {interfasse.addCustomService(i);});
			
			infos.forEach(info -> {
				log.info("	Method added [domain {}, service {}]", domain.getDomain(), info);
			});

			domain.addServicesInfos(infos);
			customInfos.addAll(infos);
		});
		
		this.servicesInfos.put(domain.getDomain(), customInfos);
	}

	private void getCustomServicesFromObject(IGGAPIDomain domain, Object customServiceProvider,
			List<IGGAPIServiceInfos> infos) {
		this.getCustomServiceFromClass(domain, customServiceProvider.getClass(), infos, () -> {
			return customServiceProvider;
		});
	}

	private void getCustomServiceFromClass(IGGAPIDomain domain, Class<?> customServiceProviderClass,
			List<IGGAPIServiceInfos> infos, IGGAPIObjectInstanciator instanciator) {
		Method[] methods = customServiceProviderClass.getDeclaredMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(GGAPICustomService.class)) {
				GGAPICustomService annotation = method.getAnnotation(GGAPICustomService.class);
				IGGAPIServiceInfos service;
				try {
					service = GGAPIServicesInfosBuilder.getInfos(domain.getDomain(), customServiceProviderClass, method,
							annotation.path(), annotation.description(), GGAPIEntityOperation.custom(domain.getDomain(),
									annotation.method(), annotation.entity(), annotation.actionOnAllEntities()),
							instanciator);
				} catch (GGAPIEngineException e) {
					throw new RuntimeException(e);
				}
				infos.add(service);
			}
		}
	}

	public IGGAPIServicesInfosRegistry getRegistry() {
		return new GGAPIServicesInfosRegistry(this.servicesInfos);
	}

}
