package com.garganttua.api.interfaces.security.spring.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringInterfaceRestSecurityApplicationFilter extends OncePerRequestFilter {

	@Autowired
	private IGGAPISecurityEngine security;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

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
			
			
			ServletInputStream stream = request.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = stream.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}

			byte[] entityAsByteArray = buffer.toByteArray();
			
			ObjectMapper mapper = new ObjectMapper();
			Object entity = mapper.readValue(entityAsByteArray, caller.getDomain().getEntity().getValue0());
			
			try {
				this.security.applySecurityOnAuthenticatorEntity(entity);
			} catch (GGAPIException e) {
				throw new IOException(e);
			}
		}

		filterChain.doFilter(request, response);
	}
}
