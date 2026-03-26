package com.garganttua.api.core.security.authentication.loginpassword;

import javax.inject.Inject;

import com.garganttua.api.spec.definition.IAuthenticatorDefinition;
import com.garganttua.api.spec.security.IPasswordEncoder;
import com.garganttua.api.spec.security.annotations.Authentication;
import com.garganttua.core.CoreException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Authentication
public class LoginPasswordAuthentication  {

	@Inject
	private IPasswordEncoder encoder;

	protected boolean authenticate(Object principal, byte[] credential, IAuthenticatorDefinition definition) throws CoreException {
		String encodedPassword = "";
		return this.encoder.matches(new String(credential), encodedPassword);
	}

/* 	@AuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(Object entity, IAuthenticatorDefinition definition) throws CoreException {
		String password = LoginPasswordEntityAuthenticatorHelper.getPassword(entity);
		if( password != null ) {
			String passwordEncoded = this.encoder.encode(password);
			LoginPasswordEntityAuthenticatorHelper.setPassword(entity, passwordEncoded);
		}
	} */
/* 
	@AuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(Object entity, IAuthenticatorDefinition definition) {
		//Nothing to do
	} */
}
