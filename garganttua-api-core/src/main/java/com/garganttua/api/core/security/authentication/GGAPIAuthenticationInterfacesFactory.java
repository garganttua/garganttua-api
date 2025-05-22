package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.service.GGAPIServicesInfosBuilder;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactory;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInfosRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterface;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorInfosRegistry;
import com.garganttua.api.spec.service.GGAPICustomService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.beans.GGBeanRefValidator;
import com.garganttua.reflection.beans.IGGBeanLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthenticationInterfacesFactory {

	private IGGAPIAuthenticatorInfosRegistry authenticatorInfosRegistry;
	private IGGAPIAuthenticationInfosRegistry authenticationInfosRegistry;
	private IGGAPIAuthenticationService authenticationService;
	private IGGBeanLoader beanLoader;
	private Map<String, IGGAPIAuthenticationInterface> authenticationInterfaces = new HashMap<String, IGGAPIAuthenticationInterface>();
//	private IGGAPIAccessRulesRegistry accessRulesRegistry;
//	private IGGAPIServicesInfosRegistry servicesInfosRegistry;
	private IGGAPIAuthenticationFactoriesRegistry authenticationFactoriesRegistry;
	private IGGAPIEngine engine;

	public GGAPIAuthenticationInterfacesFactory(IGGBeanLoader beanLoader,
			IGGAPIAuthenticatorInfosRegistry authenticatorInfosRegistry,
			IGGAPIAuthenticationInfosRegistry authenticationInfosRegistry,
			IGGAPIAuthenticationFactoriesRegistry authenticationFactoryRegistry,
			IGGAPIAuthenticationService authenticationService, 
			IGGAPIEngine engine,
			IGGAPIAuthenticationFactoriesRegistry authenticationFactoriesRegistry) {
		this.beanLoader = beanLoader;
		this.authenticatorInfosRegistry = authenticatorInfosRegistry;
		this.authenticationInfosRegistry = authenticationInfosRegistry;
		this.authenticationService = authenticationService;
		this.engine = engine;
		this.authenticationFactoriesRegistry = authenticationFactoriesRegistry;
		this.createInterfaces();
	}

	private void createInterfaces() {
		log.info("*** Creating Authentication Interfaces ...");

		this.authenticatorInfosRegistry.getDomains().forEach(domain -> {
			GGAPIAuthenticatorInfos infos = this.authenticatorInfosRegistry.getAuthenticatorInfos(domain.getDomain());
			String[] interfacesBeans = infos.authenticationInterfaces();

			for (String beanName : interfacesBeans) {
				try {
					Pair<String, String> ref = GGBeanRefValidator.validate(beanName);
					IGGAPIAuthenticationInterface authenticationInterface = (IGGAPIAuthenticationInterface) this.beanLoader
							.getBeanNamed(ref.getValue0(), ref.getValue1());

					authenticationInterface.setAuthenticationService(this.authenticationService);
					
					List<IGGAPIServiceInfos> authenticationServiceInfos = GGAPIAuthenticationServicesInfosBuilder
							.buildGGAPIServices(domain, authenticationInterface);

					for (Class<?> authenticationType : infos.authenticationTypes()) {
						GGAPIAuthenticationInfos authenticationInfos = this.authenticationInfosRegistry
								.getAuthenticationInfos(authenticationType);
						authenticationInterface.addAuthenticationInfos(authenticationInfos);

						getCustomServicesFromAuthentication(domain, authenticationInterface, authenticationServiceInfos, authenticationType, this.authenticationFactoriesRegistry.getFactory(authenticationType));
					}

					authenticationInterface.setDomain(domain);
					
					this.addServiceInfos(domain, authenticationServiceInfos);
					this.authenticationInterfaces.put(domain.getDomain(), authenticationInterface);

					log.info("	Authentication Interface added [domain {}, service {}]", domain.getDomain(),
							authenticationInterface);
				} catch (GGReflectionException | GGAPIEngineException e) {
					throw new RuntimeException(e);
				}
			}
		});

	}

	private void getCustomServicesFromAuthentication(IGGAPIDomain domain, IGGAPIAuthenticationInterface authenticationInterface,
			List<IGGAPIServiceInfos> authenticationServiceInfos, Class<?> authenticationType, IGGAPIAuthenticationFactory factory) {
		Method[] methods = authenticationType.getDeclaredMethods();

		for (Method method : methods) {
			if (method.isAnnotationPresent(GGAPICustomService.class)) {
				GGAPICustomService annotation = method.getAnnotation(GGAPICustomService.class);
				IGGAPIServiceInfos service;
				try {
					service = GGAPIServicesInfosBuilder.getInfos(domain.getDomain(),
							authenticationType, method, annotation.path(),
							annotation.description(),
							GGAPIEntityOperation.custom(domain.getDomain(), annotation.method(),
									annotation.entity(), annotation.actionOnAllEntities()), () -> {
										try {
											return factory.createDummy(domain);
										} catch (GGAPIException e) {
											throw new RuntimeException(e);
										}
									});
					authenticationInterface.addCustomService(service);
					authenticationServiceInfos.add(service);
					
				} catch (GGAPIEngineException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void addServiceInfos(IGGAPIDomain domain, List<IGGAPIServiceInfos> authenticationServiceInfos) {
		domain.addServicesInfos(authenticationServiceInfos);
		this.engine.addServicesInfos(domain, authenticationServiceInfos);
		authenticationServiceInfos.forEach(serviceInfos -> {
			try {
				this.engine.addAccessRule(domain.createAccessRule(serviceInfos));
			} catch (GGAPIException e) {
				throw new RuntimeException(e);
			}
			log.info("	Method added [domain {}, service {}]", domain.getDomain(), serviceInfos);
		});
	}

	public IGGAPIAuthenticationInterfacesRegistry getRegistry() {
		return new GGAPIAuthenticationInterfacesRegistry(this.authenticationInterfaces);
	}
}
