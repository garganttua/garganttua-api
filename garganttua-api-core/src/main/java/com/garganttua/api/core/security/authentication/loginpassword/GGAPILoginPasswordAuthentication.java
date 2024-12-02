package com.garganttua.api.core.security.authentication.loginpassword;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.authentication.GGAPILoginPasswordAuthenticatorInfos;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@GGAPIAuthentication (
	findPrincipal = true
)
@Slf4j
@NoArgsConstructor
public class GGAPILoginPasswordAuthentication extends AbstractGGAPIAuthentication {
	
	@Inject 
	private IGGAPIPasswordEncoder encoder;
	
	@Override
	protected void doAuthentication() throws GGAPIException {
		if( !GGAPIEntityAuthenticatorHelper.isAuthenticator(this.principal) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, "Authenticator as principal is mandatory for Login Password authentication, verify that findPrincipal is set to true");
		}
		String encodedPassword =  GGAPILoginPasswordEntityAuthenticatorHelper.getPassword(this.principal);
		this.authenticated = this.encoder.matches((String) this.credential, encodedPassword);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object doFindPrincipal() {
		try {
			GGAPILoginPasswordAuthenticatorInfos infos = GGAPILoginPasswordEntityAuthenticatorChecker.checkEntityAuthenticatorClass(this.authenticatorInfos.authenticatorType());
			IGGAPIServiceResponse getPrincipalResponse = this.authenticatorService.getEntities(GGAPICaller.createTenantCaller(this.tenantId), GGAPIReadOutputMode.full, null, GGAPILiteral.eq(infos.loginFieldAddress().toString(), (String) this.principal), null, new HashMap<String, String>());
			if( getPrincipalResponse.getResponseCode() == GGAPIServiceResponseCode.OK ) {
				List<Object> list = (List<Object>) getPrincipalResponse.getResponse();
				if(list.size() >0) {
					log.atDebug().log("Found principal identified by id "+this.principal);
					return list.getFirst();
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
}