package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.service.ServicesInfosBuilder;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.context.IEngine;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authentication.AuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IAuthenticationFactory;
import com.garganttua.api.spec.security.authentication.IAuthenticationInfosRegistry;
import com.garganttua.api.spec.security.authentication.IAuthenticationInterface;
import com.garganttua.api.spec.security.authentication.IAuthenticationInterfacesRegistry;
import com.garganttua.api.spec.security.authentication.IAuthenticationService;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.authenticator.IAuthenticatorInfosRegistry;
import com.garganttua.api.spec.service.CustomService;
import com.garganttua.api.spec.service.IServiceInfos;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.beans.GGBeanRefValidator;
import com.garganttua.core.reflection.beans.IGGBeanLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationInterfacesFactory {

	private IAuthenticatorInfosRegistry authenticatorInfosRegistry;
	private IAuthenticationInfosRegistry authenticationInfosRegistry;
	private IAuthenticationService authenticationService;
	private IGGBeanLoader beanLoader;
	private Map<String, IAuthenticationInterface> authenticationInterfaces = new HashMap<String, IAuthenticationInterface>();
//	private IAccessRulesRegistry accessRulesRegistry;
//	private IServicesInfosRegistry servicesInfosRegistry;
	private IAuthenticationFactoriesRegistry authenticationFactoriesRegistry;
	private IEngine engine;

	public AuthenticationInterfacesFactory(IGGBeanLoader beanLoader,
			IAuthenticatorInfosRegistry authenticatorInfosRegistry,
			IAuthenticationInfosRegistry authenticationInfosRegistry,
			IAuthenticationFactoriesRegistry authenticationFactoryRegistry,
			IAuthenticationService authenticationService, 
			IEngine engine,
			IAuthenticationFactoriesRegistry authenticationFactoriesRegistry) {
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
			AuthenticatorInfos infos = this.authenticatorInfosRegistry.getAuthenticatorInfos(domain.getDomain());
			String[] interfacesBeans = infos.authenticationInterfaces();

			for (String beanName : interfacesBeans) {
				try {
					Pair<String, String> ref = GGBeanRefValidator.validate(beanName);
					IAuthenticationInterface authenticationInterface = (IAuthenticationInterface) this.beanLoader
							.getBeanNamed(ref.getValue0(), ref.getValue1());

					authenticationInterface.setService(this.engine.getService(domain.getDomain()));
					authenticationInterface.setAuthenticationService(this.authenticationService);
					
					List<IServiceInfos> authenticationServiceInfos = AuthenticationServicesInfosBuilder
							.buildServices(domain, authenticationInterface);

					for (Class<?> authenticationType : infos.authenticationTypes()) {
						AuthenticationInfos authenticationInfos = this.authenticationInfosRegistry
								.getAuthenticationInfos(authenticationType);
						authenticationInterface.addAuthenticationInfos(authenticationInfos);

						getCustomServicesFromAuthentication(domain, authenticationInterface, authenticationServiceInfos, authenticationType, this.authenticationFactoriesRegistry.getFactory(authenticationType));
					}

					authenticationInterface.setDomain(domain);
					
					this.addServiceInfos(domain, authenticationServiceInfos);
					this.authenticationInterfaces.put(domain.getDomain(), authenticationInterface);

					log.info("	Authentication Interface added [domain {}, service {}]", domain.getDomain(),
							authenticationInterface);
				} catch (ReflectionException | EngineException e) {
					throw new RuntimeException(e);
				}
			}
		});

	}

	private void getCustomServicesFromAuthentication(IDomain domain, IAuthenticationInterface authenticationInterface,
			List<IServiceInfos> authenticationServiceInfos, Class<?> authenticationType, IAuthenticationFactory factory) {
		Method[] methods = authenticationType.getDeclaredMethods();

		for (Method method : methods) {
			if (method.isAnnotationPresent(CustomService.class)) {
				CustomService annotation = method.getAnnotation(CustomService.class);
				IServiceInfos service;
				try {
					service = ServicesInfosBuilder.getInfos(domain.getDomain(),
							authenticationType, method, annotation.path(),
							annotation.description(),
							EntityOperation.custom(domain.getDomain(), annotation.method(),
									annotation.entity(), annotation.actionOnAllEntities()), () -> {
										try {
											return factory.createDummy(domain);
										} catch (CoreException e) {
											throw new RuntimeException(e);
										}
									});
					authenticationInterface.addCustomService(service);
					authenticationServiceInfos.add(service);
					
				} catch (EngineException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void addServiceInfos(IDomain domain, List<IServiceInfos> authenticationServiceInfos) {
		domain.addServicesInfos(authenticationServiceInfos);
		this.engine.addServicesInfos(domain, authenticationServiceInfos);
		authenticationServiceInfos.forEach(serviceInfos -> {
			try {
				this.engine.addAccessRule(domain.createAccessRule(serviceInfos));
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
			log.info("	Method added [domain {}, service {}]", domain.getDomain(), serviceInfos);
		});
	}

	public IAuthenticationInterfacesRegistry getRegistry() {
		return new AuthenticationInterfacesRegistry(this.authenticationInterfaces);
	}
}
