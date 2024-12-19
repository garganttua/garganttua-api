package com.garganttua.api.core.security.authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthenticationChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInfosRegistry;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthenticationInfosFactory {

	private List<String> packages;
	private Map<Class<?>, GGAPIAuthenticationInfos> authentications = new HashMap<Class<?>, GGAPIAuthenticationInfos>();

	public GGAPIAuthenticationInfosFactory(List<String> packages) throws GGAPIEngineException {
		this.packages = packages;
		this.collectAuthentications();
	}

	private void collectAuthentications() throws GGAPIEngineException {
		log.info("*** Collecting Authentications ...");
		if (this.packages == null) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "No packages");
		}

		this.packages.forEach(packaje -> {
			this.collectAuthentications(packaje);
		});
	}

	private void collectAuthentications(String packaje) {
		log.info("Collecting Authentications in package " + packaje);
		GGObjectReflectionHelper.getClassesWithAnnotation(packaje, GGAPIAuthentication.class).forEach(entityClass -> {
			try {
				GGAPIAuthenticationInfos infos = GGAPIEntityAuthenticationChecker
						.checkEntityAuthenticationClass(entityClass);

				this.authentications.put(entityClass, infos);
				log.info("Authentication added " + infos.toString());

			} catch (GGAPIException e) {
				log.atWarn().log("Error getting infos for authentication " + entityClass.getSimpleName(), e);
			}
		});
	}

	public IGGAPIAuthenticationInfosRegistry getRegistry() {
		return new GGAPIAuthenticationInfosRegistry(this.authentications);
	}
}
