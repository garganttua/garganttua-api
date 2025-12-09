package com.garganttua.api.core.security.authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.security.entity.checker.EntityAuthenticationChecker;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.security.annotations.Authentication;
import com.garganttua.api.spec.security.authentication.AuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IAuthenticationInfosRegistry;
import com.garganttua.core.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationInfosFactory {

	private List<String> packages;
	private Map<Class<?>, AuthenticationInfos> authentications = new HashMap<Class<?>, AuthenticationInfos>();

	public AuthenticationInfosFactory(List<String> packages) throws EngineException {
		this.packages = packages;
		this.collectAuthentications();
	}

	private void collectAuthentications() throws EngineException {
		log.info("*** Collecting Authentications ...");
		if (this.packages == null) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "No packages");
		}

		this.packages.forEach(packaje -> {
			this.collectAuthentications(packaje);
		});
	}

	private void collectAuthentications(String packaje) {
		log.info("Collecting Authentications in package " + packaje);
		GGObjectReflectionHelper.getClassesWithAnnotation(packaje, Authentication.class).forEach(entityClass -> {
			try {
				AuthenticationInfos infos = EntityAuthenticationChecker
						.checkEntityAuthenticationClass(entityClass);

				this.authentications.put(entityClass, infos);
				log.info("Authentication added " + infos.toString());

			} catch (CoreException e) {
				log.atWarn().log("Error getting infos for authentication " + entityClass.getSimpleName(), e);
			}
		});
	}

	public IAuthenticationInfosRegistry getRegistry() {
		return new AuthenticationInfosRegistry(this.authentications);
	}
}
