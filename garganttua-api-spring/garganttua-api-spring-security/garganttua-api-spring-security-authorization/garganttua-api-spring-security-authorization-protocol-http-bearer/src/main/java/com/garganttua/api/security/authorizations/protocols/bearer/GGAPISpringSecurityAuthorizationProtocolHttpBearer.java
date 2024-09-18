package com.garganttua.api.security.authorizations.protocols.bearer;

import org.springframework.stereotype.Service;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.security.spring.core.authorizations.GGAPISpringSecurityAuthorizationProtocol;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringSecurityAuthorizationProtocolHttpBearer implements GGAPISpringSecurityAuthorizationProtocol {
	
	@Override
	public byte[] getAuthorization(ServletRequest request) throws GGAPIException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String authorizationHeader = httpRequest.getHeader("Authorization");
		
		if( authorizationHeader == null ) {
			log.atDebug().log("No header Authorization found");
			throw new GGAPISecurityException(GGAPIExceptionCode.BAD_REQUEST, "No header Authorization found");
		}

		if( !authorizationHeader.startsWith("Bearer ") ) {
			log.atDebug().log("Not a bearer authorization");
			throw new GGAPISecurityException(GGAPIExceptionCode.BAD_REQUEST, "Not a bearer authorization");
		}
		
		return authorizationHeader.split(" ")[1].getBytes();
	}

}
