package com.garganttua.api.core.security.authentication.pin;

import com.garganttua.api.core.context.InfosHelper;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.core.CoreException;

public class PinEntityAuthenticatorHelper {

	public static String getPin(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, PinEntityAuthenticatorChecker::checkEntityAuthenticatorClass, PinAuthenticatorInfos::pinFieldAddress);
	}

	public static void setPin(Object entity, String pin) throws CoreException {
		InfosHelper.setValue(entity, PinEntityAuthenticatorChecker::checkEntityAuthenticatorClass, PinAuthenticatorInfos::pinFieldAddress, pin);
	}

	public static int getPinSize(Object entity) throws CoreException {
		PinAuthenticatorInfos infos = PinEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());
		return infos.pinSize();
	}
	
	public static void resetPinErrorNumber(Object entity) throws CoreException {
		InfosHelper.setValue(entity, PinEntityAuthenticatorChecker::checkEntityAuthenticatorClass, PinAuthenticatorInfos::pinErrorCounterFieldAddress, 0);
	}
	
	public static int getMaxPinErrorNumber(Object entity) throws CoreException {
		PinAuthenticatorInfos infos = PinEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());
		return infos.maxPinErrorNumber();
	}

	public static void incrementPinErrorNumber(Object entity) throws CoreException {
		int pinErrorNumber = InfosHelper.getValue(entity, PinEntityAuthenticatorChecker::checkEntityAuthenticatorClass, PinAuthenticatorInfos::pinErrorCounterFieldAddress);
		pinErrorNumber++;
		if( pinErrorNumber <= getMaxPinErrorNumber(entity) )
			InfosHelper.setValue(entity, PinEntityAuthenticatorChecker::checkEntityAuthenticatorClass, PinAuthenticatorInfos::pinErrorCounterFieldAddress, pinErrorNumber);
		if( pinErrorNumber >= getMaxPinErrorNumber(entity) )
			EntityAuthenticatorHelper.setAccountNonlocked(entity, false);
	}
}
