package com.garganttua.api.core.security.authentication.loginpassword;

import com.garganttua.api.core.GGAPIInfosHelper;
import com.garganttua.api.spec.GGAPIException;

public class GGAPILoginPasswordEntityAuthenticatorHelper {
	
	public static void setPassword(Object entity, String password) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPILoginPasswordEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPILoginPasswordAuthenticatorInfos::passwordFieldAddress, password);
	}
	
	public static String getLogin(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPILoginPasswordEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPILoginPasswordAuthenticatorInfos::loginFieldAddress);
	}

	public static String getPassword(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPILoginPasswordEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPILoginPasswordAuthenticatorInfos::passwordFieldAddress);
	}
}
