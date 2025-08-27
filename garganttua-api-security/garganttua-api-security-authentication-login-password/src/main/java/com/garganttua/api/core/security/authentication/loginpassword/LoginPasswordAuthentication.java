package com.garganttua.api.core.security.authentication.loginpassword;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
public class LoginPasswordAuthentication extends AbstractAuthentication {
	
	public LoginPasswordAuthentication(IDomain domain) {
		super(domain);
	}
	
	public LoginPasswordAuthentication() {
		super(null);
	}

	@Inject 
	private IPasswordEncoder encoder;
	
	@Override
	protected void doAuthentication() throws CoreException {
		if( !EntityAuthenticatorHelper.isAuthenticator(this.principal) ) {
			throw new SecurityException(CoreExceptionCode.UNKNOWN_ERROR, "Authenticator as principal is mandatory for Login Password authentication, verify that findPrincipal is set to true");
		}
		String encodedPassword =  LoginPasswordEntityAuthenticatorHelper.getPassword(this.principal);
		this.authenticated = this.encoder.matches((String) this.credential, encodedPassword);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object doFindPrincipal(ICaller caller) {
		try {
			LoginPasswordAuthenticatorInfos infos = LoginPasswordEntityAuthenticatorChecker.checkEntityAuthenticatorClass(this.authenticatorInfos.authenticatorType());
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
	
	@AuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) throws CoreException {
		String password = LoginPasswordEntityAuthenticatorHelper.getPassword(entity);
		if( password != null ) {
			String passwordEncoded = this.encoder.encode(password);
			LoginPasswordEntityAuthenticatorHelper.setPassword(entity, passwordEncoded);
		}
	}
	
	@AuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) {
		//Nothing to do
	}
}