package com.garganttua.api.core.security.authentication.pin;

import com.garganttua.api.core.GGAPIInfosHelper;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.spec.GGAPIException;

public class GGAPIPinEntityAuthenticatorHelper {

	public static String getPin(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIPinEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIPinAuthenticatorInfos::pinFieldAddress);
	}

	public static void setPin(Object entity, String pin) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIPinEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIPinAuthenticatorInfos::pinFieldAddress, pin);
	}

	public static int getPinSize(Object entity) throws GGAPIException {
		GGAPIPinAuthenticatorInfos infos = GGAPIPinEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());
		return infos.pinSize();
	}
	
	public static void resetPinErrorNumber(Object entity) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIPinEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIPinAuthenticatorInfos::pinErrorCounterFieldAddress, 0);
	}
	
	public static int getMaxPinErrorNumber(Object entity) throws GGAPIException {
		GGAPIPinAuthenticatorInfos infos = GGAPIPinEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());
		return infos.maxPinErrorNumber();
	}

	public static void incrementPinErrorNumber(Object entity) throws GGAPIException {
		int pinErrorNumber = GGAPIInfosHelper.getValue(entity, GGAPIPinEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIPinAuthenticatorInfos::pinErrorCounterFieldAddress);
		pinErrorNumber++;
		if( pinErrorNumber <= getMaxPinErrorNumber(entity) )
			GGAPIInfosHelper.setValue(entity, GGAPIPinEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIPinAuthenticatorInfos::pinErrorCounterFieldAddress, pinErrorNumber);
		if( pinErrorNumber >= getMaxPinErrorNumber(entity) )
			GGAPIEntityAuthenticatorHelper.setAccountNonlocked(entity, false);
	}
}
