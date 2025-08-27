package com.garganttua.api.core.security.entity.checker;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.security.EntitySecurityInfos;
import com.garganttua.api.spec.security.annotations.Authenticator;
import com.garganttua.api.spec.security.annotations.Authorization;
import com.garganttua.api.spec.security.annotations.EntitySecurity;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.authorization.AuthorizationInfos;
import com.garganttua.api.spec.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.spec.service.ServiceAccess;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntitySecurityChecker {

	private static Map<Class<?>, EntitySecurityInfos> infos = new HashMap<Class<?>, EntitySecurityInfos>();

	public static EntitySecurityInfos checkEntityClass(Class<?> entityClass, String domainName) throws CoreException {
		if (EntitySecurityChecker.infos.containsKey(entityClass)) {
			return EntitySecurityChecker.infos.get(entityClass);
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity security infos from class " + entityClass.getSimpleName());
		}
		EntitySecurityInfos infos = null;
		EntitySecurity annotation = entityClass.getAnnotation(EntitySecurity.class);
		AuthenticatorInfos authenticatorInfos = null;
		AuthorizationInfos authorizationInfos = null;

		if (entityClass.getAnnotation(Authenticator.class) != null) {
			authenticatorInfos = EntityAuthenticatorChecker.checkEntityAuthenticatorClass(entityClass);
		}
		if (entityClass.getAnnotation(Authorization.class) != null) {
			authorizationInfos = EntityAuthorizationChecker.checkEntityAuthorizationClass(entityClass);
		}

		if (annotation == null) {
			infos = new EntitySecurityInfos(
					ServiceAccess.tenant,
					ServiceAccess.tenant,
					ServiceAccess.tenant,
					ServiceAccess.tenant,
					ServiceAccess.tenant,
					ServiceAccess.tenant,
					ServiceAccess.tenant,
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					authenticatorInfos == null ? false : true,
					authenticatorInfos == null ? null : authenticatorInfos.scope(),
					authorizationInfos == null ? false : true,
					null,
					null,
					domainName);
		} else {
			Class<?>[] authorizationProtocols = annotation.authorizationProtocols();
			for (Class<?> protocol : authorizationProtocols) {
				if (!IAuthorizationProtocol.class.isAssignableFrom(protocol)) {
					throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "The protocol "
							+ protocol.getSimpleName() + " must implements the IAuthorizationProtocol interface");
				}
				try {
					protocol.getDeclaredConstructor();
				} catch (NoSuchMethodException e) {
					throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
							"The protocol " + protocol.getSimpleName() + " must have one constructor with no params");
				}
			}

			infos = new EntitySecurityInfos(
					annotation.creation_access(),
					annotation.read_all_access(),
					annotation.read_one_access(),
					annotation.update_one_access(),
					annotation.delete_all_access(),
					annotation.delete_one_access(),
					annotation.count_access(),
					annotation.creation_access() == ServiceAccess.anonymous ? false : annotation.creation_authority(),
					annotation.read_all_access() == ServiceAccess.anonymous ? false : annotation.read_all_authority(),
					annotation.read_one_access() == ServiceAccess.anonymous ? false : annotation.read_one_authority(),
					annotation.update_one_access() == ServiceAccess.anonymous ? false
							: annotation.update_one_authority(),
					annotation.delete_all_access() == ServiceAccess.anonymous ? false
							: annotation.delete_all_authority(),
					annotation.delete_one_access() == ServiceAccess.anonymous ? false
							: annotation.delete_one_authority(),
					annotation.count_access() == ServiceAccess.anonymous ? false : annotation.count_authority(),
					authenticatorInfos == null ? false : true,
					authenticatorInfos == null ? null : authenticatorInfos.scope(),
					authorizationInfos == null ? false : true,
					annotation.authorizations(),
					annotation.authorizationProtocols(),
					domainName);
		}
		EntitySecurityChecker.infos.put(entityClass, infos);
		return infos;
	}

}
