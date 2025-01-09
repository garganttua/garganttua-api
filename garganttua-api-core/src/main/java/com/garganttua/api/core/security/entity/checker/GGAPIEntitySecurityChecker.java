package com.garganttua.api.core.security.entity.checker;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.GGAPIEntitySecurityInfos;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorization;
import com.garganttua.api.spec.security.annotations.GGAPIEntitySecurity;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.authorization.GGAPIAuthorizationInfos;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationProtocol;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntitySecurityChecker {
	
	private static Map<Class<?>, GGAPIEntitySecurityInfos> infos = new HashMap<Class<?>, GGAPIEntitySecurityInfos>();

	public static GGAPIEntitySecurityInfos checkEntityClass(Class<?> entityClass, String domainName) throws GGAPIException {
		if( GGAPIEntitySecurityChecker.infos.containsKey(entityClass) ) {
			return GGAPIEntitySecurityChecker.infos.get(entityClass);
		}
		
		if (log.isDebugEnabled()) { 
			log.debug("Checking entity security infos from class " + entityClass.getSimpleName());
		}
		GGAPIEntitySecurityInfos infos = null;
		GGAPIEntitySecurity annotation = entityClass.getAnnotation(GGAPIEntitySecurity.class);
		GGAPIAuthenticatorInfos authenticatorInfos = null;
		GGAPIAuthorizationInfos authorizationInfos = null;
		
		if( entityClass.getAnnotation(GGAPIAuthenticator.class) != null ) {
			authenticatorInfos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entityClass);
		}
		if( entityClass.getAnnotation(GGAPIAuthorization.class) != null ) {
			authorizationInfos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(entityClass); 
		}

		if( annotation == null ) {
			infos = new GGAPIEntitySecurityInfos(
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				true, 
				true, 
				true, 
				true, 
				true, 
				true, 
				true,
				authenticatorInfos==null?false:true,
				authenticatorInfos==null?null:authenticatorInfos.scope(),
				authorizationInfos==null?false:true,
				null,
				null,
				domainName
			);
		} else {
			Class<?>[] authorizationProtocols = annotation.authorizationProtocols();
			for( Class<?> protocol: authorizationProtocols) {
				if( !IGGAPIAuthorizationProtocol.class.isAssignableFrom(protocol) ) {
					throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "The protocol "+protocol.getSimpleName()+" must implements the IGGAPIAuthorizationProtocol interface");
				}
				try {
					protocol.getDeclaredConstructor();
				} catch (NoSuchMethodException | SecurityException e) {
					throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "The protocol "+protocol.getSimpleName()+" must have one constructor with no params");
				}
			}

			infos = new GGAPIEntitySecurityInfos(
				annotation.creation_access(), 
				annotation.read_all_access(), 
				annotation.read_one_access(), 
				annotation.update_one_access(), 
				annotation.delete_all_access(), 
				annotation.delete_one_access(), 
				annotation.count_access(), 
				annotation.creation_access()==GGAPIServiceAccess.anonymous?false:annotation.creation_authority(), 
				annotation.read_all_access()==GGAPIServiceAccess.anonymous?false:annotation.read_all_authority(),
				annotation.read_one_access()==GGAPIServiceAccess.anonymous?false:annotation.read_one_authority(), 
				annotation.update_one_access()==GGAPIServiceAccess.anonymous?false:annotation.update_one_authority(), 
				annotation.delete_all_access()==GGAPIServiceAccess.anonymous?false:annotation.delete_all_authority(), 
				annotation.delete_one_access()==GGAPIServiceAccess.anonymous?false:annotation.delete_one_authority(), 
				annotation.count_access()==GGAPIServiceAccess.anonymous?false:annotation.count_authority(),
				authenticatorInfos==null?false:true,
				authenticatorInfos==null?null:authenticatorInfos.scope(),
				authorizationInfos==null?false:true,
				annotation.authorizations(),
				annotation.authorizationProtocols(),
				domainName
			);
		}
		GGAPIEntitySecurityChecker.infos.put(entityClass, infos);
		return infos;
	}

}
