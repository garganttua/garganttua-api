package com.garganttua.api.interfaces.security.spring.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPISpringHttpApiFilter;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringInterfaceRestSecurityApplicationFilter extends GGAPISpringHttpApiFilter {

	@Autowired
	private IGGAPISecurityEngine security;
	
	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response) throws GGAPIException {
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerFilter.CALLER_ATTRIBUTE_NAME);
		
		if( caller.getDomain().getSecurity().isAuthenticatorEntity() 
				&& (
						request.getMethod().equals("POST") 
						|| request.getMethod().equals("PATCH") 
						|| request.getMethod().equals("PUT")
					) 
		) {
			if( log.isDebugEnabled() ) {
				log.debug("Applying security on authenticator entity "+caller.getDomain().getEntity().getValue0().getSimpleName());
			}
			
			try {
				ServletInputStream stream = request.getInputStream();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				
				int nRead;
				byte[] data = new byte[16384];
				
				while ((nRead = stream.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				
				byte[] entityAsByteArray = buffer.toByteArray();
				
				ObjectMapper mapper = new ObjectMapper();
				Object entity;
				entity = mapper.readValue(entityAsByteArray, caller.getDomain().getEntity().getValue0());
			
				this.security.applySecurityOnAuthenticatorEntity(entity);
			} catch (IOException e) {
				throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, e.getMessage());
			}

		}
	}
}
