package com.garganttua.api.core.security.authorization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.security.entity.checker.EntityAuthorizationChecker;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.security.annotations.Authorization;
import com.garganttua.api.spec.security.authorization.AuthorizationInfos;
import com.garganttua.api.spec.security.authorization.IAuthorizationInfosRegistry;
import com.garganttua.core.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationInfosFactory {

	private List<String> packages;
	private Map<Class<?>, AuthorizationInfos> authorizations = new HashMap<Class<?>, AuthorizationInfos>();

	public AuthorizationInfosFactory(List<String> packages) throws EngineException {
		this.packages = packages;
		this.collectAuthorizations();
	}

	private void collectAuthorizations() throws EngineException {
		log.info("*** Collecting Authorizations ...");
		if (this.packages == null) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "No packages");
		}

		this.packages.forEach(packaje -> {
			this.collectAuthorizations(packaje);
		});
	}

	private void collectAuthorizations(String packaje) {
		log.info("Collecting Authorizations in package " + packaje);
		GGObjectReflectionHelper.getClassesWithAnnotation(packaje, Authorization.class).forEach(entityClass -> {
			try {
				AuthorizationInfos infos = EntityAuthorizationChecker
						.checkEntityAuthorizationClass(entityClass);

				this.authorizations.put(entityClass, infos);
				log.info("Authorization added " + infos.toString());

			} catch (CoreException e) {
				log.atWarn().log("Error getting infos for authorization " + entityClass.getSimpleName(), e);
			}
		});
	}

	public IAuthorizationInfosRegistry getRegistry() {
		return new AuthorizationInfosRegistry(this.authorizations);
	}

}
