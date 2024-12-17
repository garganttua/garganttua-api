package com.garganttua.api.core.security.authentication.pin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPIPasswordEncoder;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import lombok.extern.slf4j.Slf4j;

@GGAPIAuthentication (
		findPrincipal = true
	)
@Slf4j
public class GGAPIPinAuthentication extends AbstractGGAPIAuthentication {
	
	public GGAPIPinAuthentication(IGGAPIDomain domain) {
		super(domain);
	}
	
	public GGAPIPinAuthentication() {
		super(null);
	}

	@Inject 
	private IGGAPIPasswordEncoder encoder;
	
	@Override
	protected Object doFindPrincipal(IGGAPICaller caller) {
		try {
			GGAPIPinAuthenticatorInfos infos = GGAPIPinEntityAuthenticatorChecker.checkEntityAuthenticatorClass(this.authenticatorInfos.authenticatorType());
			IGGAPIServiceResponse getPrincipalResponse = this.authenticatorService.getEntities(caller, GGAPIReadOutputMode.full, null, GGAPILiteral.eq(infos.loginFieldAddress().toString(), (String) this.principal), null, new HashMap<String, String>());
			if( getPrincipalResponse.getResponseCode() == GGAPIServiceResponseCode.OK ) {
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
		} catch (GGAPIException e) {
			log.atDebug().log("Failed to find principal identified by id "+this.principal, e);
			return null;
		}
	}

	@Override
	protected void doAuthentication() throws GGAPIException {
		if( !GGAPIEntityAuthenticatorHelper.isAuthenticator(this.principal) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, "Authenticator as principal is mandatory for Pin authentication, verify that findPrincipal is set to true");
		}
		String encodedPin = GGAPIPinEntityAuthenticatorHelper.getPin(this.principal);
		this.authenticated = this.encoder.matches((String) this.credential, encodedPin);
		
		if( !this.authenticated ) {
			GGAPIPinEntityAuthenticatorHelper.incrementPinErrorNumber(this.principal);
		} else {
			GGAPIPinEntityAuthenticatorHelper.resetPinErrorNumber(this.principal);
		}
		GGAPIEntityHelper.save(this.principal, GGAPICaller.createTenantCaller(this.tenantId), new HashMap<String, String>());
	}
	
	@GGAPIAuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) throws GGAPIException {
		String pin = GGAPIPinEntityAuthenticatorHelper.getPin(entity);
		int pinSize = GGAPIPinEntityAuthenticatorHelper.getPinSize(entity);
		if( pin != null ) {
			isValidPin(pin, pinSize);
			String passwordEncoded = this.encoder.encode(pin);
			GGAPIPinEntityAuthenticatorHelper.setPin(entity, passwordEncoded);
		}
	}
	
	@GGAPIAuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) {
		//Nothing to do
	}
	
	public static boolean isValidPin(String pin, int size) throws GGAPIEngineException {
		if (pin == null || pin.length() != 4) {
			throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "Invalid code pin "+pin);
		}

		for (char c : pin.toCharArray()) {
			if (!Character.isDigit(c)) {
				throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "Invalid code pin "+pin);
			}
		}
		return true;
	}

}
