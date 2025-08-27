package com.garganttua.api.security.authorizations.protocols.bearer;

import org.springframework.stereotype.Service;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.security.authorization.IAuthorizationProtocol;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SpringSecurityAuthorizationProtocolHttpBearer implements IAuthorizationProtocol {

	@Override
	public byte[] getAuthorization(Object request) throws CoreException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String authorizationHeader = httpRequest.getHeader("Authorization");
		
		if( authorizationHeader == null ) {
			log.atDebug().log("No header Authorization found");
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "No header Authorization found");
		}

		if( !authorizationHeader.startsWith("Bearer ") ) {
			log.atDebug().log("Not a bearer authorization");
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Not a bearer authorization");
		}
		return authorizationHeader.split(" ")[1].getBytes();
	}

	@Override
	public void setAuthorization(byte[] authorization, Object response) throws CoreException {
		if( authorization == null )
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authorization is null");
		String bearer = "Bearer "+new String(authorization);
		((HttpServletResponse) response).addHeader("Authorization", bearer);
		((HttpServletResponse) response).addHeader("Access-Control-Expose-Headers", "Authorization");
	}

	@Override
	public String getProtocol() {
		return "Bearer";
	}

}
