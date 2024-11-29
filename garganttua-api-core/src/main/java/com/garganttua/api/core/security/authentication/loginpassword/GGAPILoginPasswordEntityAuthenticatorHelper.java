package com.garganttua.api.core.security.authentication.loginpassword;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.authentication.GGAPILoginPasswordAuthenticatorInfos;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class GGAPILoginPasswordEntityAuthenticatorHelper {
	
	public static void setPassword(Object entity, String password) throws GGAPIException {
		GGAPILoginPasswordAuthenticatorInfos infos = GGAPILoginPasswordEntityAuthenticatorChecker.checkEntityAuthenticator(entity.getClass());
		if( infos.passwordFieldAddress() != null ) {
			try {
				GGObjectQueryFactory.objectQuery(entity).setValue(infos.passwordFieldAddress(), password);
			} catch (GGReflectionException e) {
				GGAPIException.processException(e);
			}	
		}
	}
	
	public static String getLogin(Object entity) throws GGAPIException {
		GGAPILoginPasswordAuthenticatorInfos infos = GGAPILoginPasswordEntityAuthenticatorChecker.checkEntityAuthenticator(entity.getClass());
		if( infos.loginFieldAddress() != null ) {
			try {
				return (String) GGObjectQueryFactory.objectQuery(entity).getValue(infos.loginFieldAddress());
			} catch (GGReflectionException e) {
				GGAPIException.processException(e);
			}
		}
		return null; 
	}

	public static String getPassword(Object entity) throws GGAPIException {
		GGAPILoginPasswordAuthenticatorInfos infos = GGAPILoginPasswordEntityAuthenticatorChecker.checkEntityAuthenticator(entity.getClass());
		if( infos.passwordFieldAddress() != null ) {
			try {
				return (String) GGObjectQueryFactory.objectQuery(entity).getValue(infos.passwordFieldAddress());
			} catch (GGReflectionException e) {
				GGAPIException.processException(e);
			}
		}
		return null; 
	}
}
