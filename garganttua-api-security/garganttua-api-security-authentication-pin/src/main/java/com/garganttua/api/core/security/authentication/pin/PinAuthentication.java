package com.garganttua.api.core.security.authentication.pin;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.authentication.AbstractAuthentication;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.commons.CoreExceptionCode;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.security.IPasswordEncoder;
import com.garganttua.api.commons.security.annotations.Authentication;
import com.garganttua.api.commons.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.commons.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.CoreException;

import lombok.extern.slf4j.Slf4j;

@Authentication (
		findPrincipal = true
	)
@Slf4j
public class PinAuthentication extends AbstractAuthentication {

	public PinAuthentication(IDomain<?> domainContext) {
		super(domainContext);
	}

	public PinAuthentication() {
		super(null);
	}

	@Inject
	private IPasswordEncoder encoder;

	@SuppressWarnings("unchecked")
	@Override
	protected Object doFindPrincipal(ICaller caller) {
		try {
			PinAuthenticatorInfos infos = PinEntityAuthenticatorChecker.checkEntityAuthenticatorClass((Class<?>) this.authenticatorInfos.authenticatorType().getType());
			IOperationResponse response = this.authenticatorDomain.readAll(
				Filter.eq(infos.loginFieldAddress().toString(), (String) this.principal), null, null, caller);
			if( response.getResponseCode() == OperationResponseCode.OK ) {
				List<Object> list = (List<Object>) response.getResponse();
				if(list.size() >0) {
					log.atDebug().log("Found principal identified by id "+this.principal);
					return list.get(0);
				} else {
					log.atDebug().log("Failed to find principal identified by id "+this.principal);
					return null;
				}
			} else {
				log.atDebug().log("Failed to find principal identified by id "+this.principal);
				return null;
			}
		} catch (CoreException e) {
			log.atDebug().log("Failed to find principal identified by id "+this.principal, e);
			return null;
		}
	}

	@Override
	protected void doAuthentication() throws CoreException {
		if( !EntityAuthenticatorHelper.isAuthenticator(this.principal) ) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authenticator as principal is mandatory for Pin authentication, verify that findPrincipal is set to true");
		}
		String encodedPin = PinEntityAuthenticatorHelper.getPin(this.principal);
		this.authenticated = this.encoder.matches((String) this.credential, encodedPin);

		if( !this.authenticated ) {
			PinEntityAuthenticatorHelper.incrementPinErrorNumber(this.principal);
		} else {
			PinEntityAuthenticatorHelper.resetPinErrorNumber(this.principal);
		}
		String uuid = (String) DefaultMapper.reflection().getFieldValue(this.principal, this.authenticatorDomain.getUuidFieldAddress());
		this.authenticatorDomain.updateOne(uuid, this.principal, Caller.createTenantCaller(this.tenantId));
	}

	@AuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) throws CoreException {
		String pin = PinEntityAuthenticatorHelper.getPin(entity);
		int pinSize = PinEntityAuthenticatorHelper.getPinSize(entity);
		if( pin != null ) {
			isValidPin(pin, pinSize);
			String passwordEncoded = this.encoder.encode(pin);
			PinEntityAuthenticatorHelper.setPin(entity, passwordEncoded);
		}
	}

	@AuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) {
		//Nothing to do
	}

	public static boolean isValidPin(String pin, int size) throws SecurityException {
		if (pin == null || pin.length() != size) {
			throw new SecurityException(CoreExceptionCode.BAD_REQUEST, "Invalid code pin "+pin);
		}

		for (char c : pin.toCharArray()) {
			if (!Character.isDigit(c)) {
				throw new SecurityException(CoreExceptionCode.BAD_REQUEST, "Invalid code pin "+pin);
			}
		}
		return true;
	}

}
