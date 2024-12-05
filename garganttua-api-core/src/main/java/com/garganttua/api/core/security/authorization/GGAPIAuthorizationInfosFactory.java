package com.garganttua.api.core.security.authorization;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthorizationChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorization;
import com.garganttua.api.spec.security.authorization.GGAPIAuthorizationInfos;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationInfosRegistry;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthorizationInfosFactory {

	private List<String> packages;
	private Map<Class<?>, GGAPIAuthorizationInfos> authorizations = new HashMap<Class<?>, GGAPIAuthorizationInfos>();


	public GGAPIAuthorizationInfosFactory(List<String> packages) throws GGAPIEngineException {
		this.packages = packages;
		this.collectAuthorizations();
	}

	private void collectAuthorizations() throws GGAPIEngineException {
		log.info("*** Collecting Authorizations ...");
		if (this.packages == null) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "No packages");
		}
		
		this.packages.forEach(packaje -> {
			this.collectAuthorizations(packaje);
		});
	}

	private void collectAuthorizations(String packaje) {
		log.info("Collecting Authorizations in package "+packaje);
		try {
			GGObjectReflectionHelper.getClassesWithAnnotation(packaje, GGAPIAuthorization.class).forEach(entityClass -> {
				try {
					GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(entityClass);

					this.authorizations.put(entityClass, infos);
					log.info("Authorization added "+infos.toString());
					
				} catch (GGAPIException e) {
					log.atWarn().log("Error getting infos for authorization "+entityClass.getSimpleName(), e);
				}
			});

		} catch (ClassNotFoundException | IOException e) {
			log.atWarn().log("Error getting infos for authorization in package "+packaje, e);
		}
	}

	public IGGAPIAuthorizationInfosRegistry getRegistry() {
		return new GGAPIAuthorizationInfosRegistry(this.authorizations);
	}

}
