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
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInfosRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterface;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationServicesRegistry;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IGGAPIAuthenticatorInfosRegistry;
import com.garganttua.api.spec.service.GGAPICustomService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.api.spec.service.IGGAPIServicesInfosRegistry;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.beans.GGBeanRefValidator;
import com.garganttua.reflection.beans.IGGBeanLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthenticationInterfacesFactory {

	private IGGAPIAuthenticatorInfosRegistry authenticatorInfosRegistry;
	private IGGAPIAuthenticationInfosRegistry authenticationInfosRegistry;
	private IGGAPIAuthenticationServicesRegistry authenticationServicesRegistry;
	private IGGBeanLoader beanLoader;
	private Map<String, IGGAPIAuthenticationInterface> authenticationInterfaces = new HashMap<String, IGGAPIAuthenticationInterface>();
	private IGGAPIAccessRulesRegistry accessRulesRegistry;
	private IGGAPIServicesInfosRegistry servicesInfosRegistry;

	public GGAPIAuthenticationInterfacesFactory(IGGBeanLoader beanLoader, IGGAPIAuthenticatorInfosRegistry authenticatorInfosRegistry,
			IGGAPIAuthenticationInfosRegistry authenticationInfosRegistry,
			IGGAPIAuthenticationFactoriesRegistry authenticationFactoryRegistry,
			IGGAPIAuthenticationServicesRegistry authenticationServicesRegistry,
			IGGAPIAccessRulesRegistry accessRulesRegistry, 
			IGGAPIServicesInfosRegistry servicesInfosRegistry) {
		this.beanLoader = beanLoader;
		this.authenticatorInfosRegistry = authenticatorInfosRegistry;
		this.authenticationInfosRegistry = authenticationInfosRegistry;
		this.authenticationServicesRegistry = authenticationServicesRegistry;
		this.accessRulesRegistry = accessRulesRegistry;
		this.servicesInfosRegistry = servicesInfosRegistry;
		this.createInterfaces();
	}

	private void createInterfaces() {
		log.info("*** Creating Authentication Interfaces ...");
		
		this.authenticatorInfosRegistry.getDomains().forEach(domain -> {
			GGAPIAuthenticatorInfos infos = this.authenticatorInfosRegistry.getAuthenticatorInfos(domain.getDomain());
			Class<?> authentication = infos.authenticationType();
			String[] interfacesBeans = infos.authenticationInterfaces();
			
			GGAPIAuthenticationInfos authenticationInfos = this.authenticationInfosRegistry.getAuthenticationInfos(authentication);
			IGGAPIAuthenticationService authenticationService = this.authenticationServicesRegistry.getService(authentication);
			
			for( String beanName: interfacesBeans ) {
				try {
					Pair<String, String> ref = GGBeanRefValidator.validate(beanName);
					IGGAPIAuthenticationInterface authenticationInterface = (IGGAPIAuthenticationInterface) this.beanLoader.getBeanNamed(ref.getValue0(), ref.getValue1());
					
					authenticationInterface.setAuthenticationService(authenticationService);
					authenticationInterface.setAuthenticationInfos(authenticationInfos);
					authenticationInterface.setDomain(domain);
					List<IGGAPIServiceInfos> authenticationServiceInfos = GGAPIAuthenticationServicesInfosBuilder.buildGGAPIServices(domain, authenticationInfos, authenticationInterface);
					

					Method[] methods = authenticationInterface.getClass().getDeclaredMethods();
					
					for (Method method : methods) {
						if (method.isAnnotationPresent(GGAPICustomService.class)) {
							GGAPICustomService annotation = method.getAnnotation(GGAPICustomService.class);
							IGGAPIServiceInfos service;
							try {
								service = GGAPIServicesInfosBuilder.getInfos(domain.getDomain(), authenticationInterface.getClass(),
										method, annotation.path(), annotation.description(),
										GGAPIEntityOperation.custom(domain.getDomain(), annotation.method(), annotation.entity(), annotation.actionOnAllEntities()));
								authenticationServiceInfos.add(service);
							} catch (GGAPIEngineException e) {
								throw new RuntimeException(e);
							}
						}
					}
					
					this.addServiceInfos(domain, authenticationServiceInfos);
					this.authenticationInterfaces.put(domain.getDomain(), authenticationInterface);
					
					log.info("	Authentication Interface added [domain {}, service {}]", domain.getDomain(), authenticationInterface);
				} catch (GGReflectionException | GGAPIEngineException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
	}

	private void addServiceInfos(IGGAPIDomain domain, List<IGGAPIServiceInfos> authenticationServiceInfos) {
		domain.addServicesInfos(authenticationServiceInfos);
		this.servicesInfosRegistry.addServicesInfos(domain, authenticationServiceInfos);
		authenticationServiceInfos.forEach(serviceInfos -> {
			try {
				this.accessRulesRegistry.addAccessRule(domain.createAccessRule(serviceInfos));
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
