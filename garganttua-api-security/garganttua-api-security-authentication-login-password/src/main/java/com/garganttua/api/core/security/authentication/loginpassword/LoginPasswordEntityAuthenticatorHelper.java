package com.garganttua.api.core.security.authentication.loginpassword;

import com.garganttua.api.core.context.InfosHelper;
import com.garganttua.core.CoreException;

public class LoginPasswordEntityAuthenticatorHelper {
	
	public static void setPassword(Object entity, String password) throws CoreException {
		InfosHelper.setValue(entity, LoginPasswordEntityAuthenticatorChecker::checkEntityAuthenticatorClass, LoginPasswordAuthenticatorInfos::passwordFieldAddress, password);
	}
	
	public static String getLogin(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, LoginPasswordEntityAuthenticatorChecker::checkEntityAuthenticatorClass, LoginPasswordAuthenticatorInfos::loginFieldAddress);
	}

	public static String getPassword(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, LoginPasswordEntityAuthenticatorChecker::checkEntityAuthenticatorClass, LoginPasswordAuthenticatorInfos::passwordFieldAddress);
	}
}
