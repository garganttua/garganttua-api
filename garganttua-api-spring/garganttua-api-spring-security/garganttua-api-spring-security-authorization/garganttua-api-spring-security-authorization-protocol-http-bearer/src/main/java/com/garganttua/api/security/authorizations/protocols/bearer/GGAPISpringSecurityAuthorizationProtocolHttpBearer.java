package com.garganttua.api.security.authorizations.protocols.bearer;

import org.springframework.stereotype.Service;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationProtocol;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringSecurityAuthorizationProtocolHttpBearer implements IGGAPIAuthorizationProtocol {

	@Override
	public byte[] getAuthorization(Object request) throws GGAPIException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String authorizationHeader = httpRequest.getHeader("Authorization");
		
		if( authorizationHeader == null ) {
			log.atDebug().log("No header Authorization found");
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "No header Authorization found");
		}

		if( !authorizationHeader.startsWith("Bearer ") ) {
			log.atDebug().log("Not a bearer authorization");
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Not a bearer authorization");
		}
		return authorizationHeader.split(" ")[1].getBytes();
	}

	@Override
	public void setAuthorization(byte[] authorization, Object response) throws GGAPIException {
		if( authorization == null )
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Authorization is null");
		String bearer = "Bearer "+new String(authorization);
		((HttpServletResponse) response).addHeader("Authorization", bearer);
		((HttpServletResponse) response).addHeader("Access-Control-Expose-Headers", "Authorization");
	}

	@Override
	public String getProtocol() {
		return "Bearer";
	}

}
