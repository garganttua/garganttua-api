package com.garganttua.api.core.security.authentication.pin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.api.core.filter.Literal;
import com.garganttua.api.core.security.authentication.AbstractAuthentication;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.IPasswordEncoder;
import com.garganttua.api.spec.security.annotations.Authentication;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.service.ReadOutputMode;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.service.IServiceResponse;

import lombok.extern.slf4j.Slf4j;

@Authentication (
		findPrincipal = true
	)
@Slf4j
public class PinAuthentication extends AbstractAuthentication {
	
	public PinAuthentication(IDomain domain) {
		super(domain);
	}
	
	public PinAuthentication() {
		super(null);
	}

	@Inject 
	private IPasswordEncoder encoder;
	
	@Override
	protected Object doFindPrincipal(ICaller caller) {
		try {
			PinAuthenticatorInfos infos = PinEntityAuthenticatorChecker.checkEntityAuthenticatorClass(this.authenticatorInfos.authenticatorType());
			IServiceResponse getPrincipalResponse = this.authenticatorService.getEntities(caller, ReadOutputMode.full, null, Literal.eq(infos.loginFieldAddress().toString(), (String) this.principal), null, new HashMap<String, String>());
			if( getPrincipalResponse.getResponseCode() == ServiceResponseCode.OK ) {
				List<Object> list = (List<Object>) getPrincipalResponse.getResponse();
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
			throw new SecurityException(CoreExceptionCode.UNKNOWN_ERROR, "Authenticator as principal is mandatory for Pin authentication, verify that findPrincipal is set to true");
		}
		String encodedPin = PinEntityAuthenticatorHelper.getPin(this.principal);
		this.authenticated = this.encoder.matches((String) this.credential, encodedPin);
		
		if( !this.authenticated ) {
			PinEntityAuthenticatorHelper.incrementPinErrorNumber(this.principal);
		} else {
			PinEntityAuthenticatorHelper.resetPinErrorNumber(this.principal);
		}
		EntityHelper.save(this.principal, Caller.createTenantCaller(this.tenantId), new HashMap<String, String>());
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
	
	public static boolean isValidPin(String pin, int size) throws EngineException {
		if (pin == null || pin.length() != 4) {
			throw new EngineException(CoreExceptionCode.BAD_REQUEST, "Invalid code pin "+pin);
		}

		for (char c : pin.toCharArray()) {
			if (!Character.isDigit(c)) {
				throw new EngineException(CoreExceptionCode.BAD_REQUEST, "Invalid code pin "+pin);
			}
		}
		return true;
	}

}
