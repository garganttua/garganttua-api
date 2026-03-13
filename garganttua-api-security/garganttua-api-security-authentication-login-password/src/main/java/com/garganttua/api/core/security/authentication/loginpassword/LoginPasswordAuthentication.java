package com.garganttua.api.core.security.authentication.loginpassword;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.security.authentication.AbstractAuthentication;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.security.IPasswordEncoder;
import com.garganttua.api.spec.security.annotations.Authentication;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.core.CoreException;

import lombok.extern.slf4j.Slf4j;

@Authentication (
	findPrincipal = true
)
@Slf4j
public class LoginPasswordAuthentication extends AbstractAuthentication {

	public LoginPasswordAuthentication(IDomainContext<?> domainContext) {
		super(domainContext);
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
			LoginPasswordAuthenticatorInfos infos = LoginPasswordEntityAuthenticatorChecker.checkEntityAuthenticatorClass((Class<?>) this.authenticatorInfos.authenticatorType().getType());
			IOperationResponse response = this.authenticatorDomainContext.readAll(
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
