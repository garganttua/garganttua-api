package com.garganttua.api.core.security.authentication.loginpassword;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.spec.GGAPIException;
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
		String encodedPassword =  GGAPILoginPasswordEntityAuthenticatorHelper.getPassword(this.principal);
		this.authenticated = this.encoder.matches((String) this.credential, encodedPassword);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object findPrincipal() {
		try {
			
			GGAPILoginPasswordAuthenticatorInfos infos = GGAPILoginPasswordEntityAuthenticatorChecker.checkEntityAuthenticatorClass(this.authenticatorInfos.authenticatorType());
			IGGAPIServiceResponse getPrincipalResponse = this.authenticatorService.getEntities(GGAPICaller.createTenantCaller(this.tenantId), GGAPIReadOutputMode.full, null, GGAPILiteral.eq(infos.loginFieldAddress().toString(), (String) this.principal), null, new HashMap<String, String>());
			if( getPrincipalResponse.getResponseCode() == GGAPIServiceResponseCode.OK ) {
				List<Object> list = (List<Object>) getPrincipalResponse.getResponse();
				if(list.size() >0) {
					return list.getFirst();
				} else {
					return null;
				}
			} else {
				log.atDebug().log("Failed to find principal");
				return null;
			}	
		} catch (GGAPIException e) {
			log.atDebug().log("Failed to find principal", e);
			return null;
		}
	}
}