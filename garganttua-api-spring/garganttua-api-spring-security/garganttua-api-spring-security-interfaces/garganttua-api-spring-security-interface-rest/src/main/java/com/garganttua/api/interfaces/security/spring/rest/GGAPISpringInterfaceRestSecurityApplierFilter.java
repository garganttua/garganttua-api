package com.garganttua.api.interfaces.security.spring.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.service.GGAPIServiceResponse;
import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPIServiceResponseUtils;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.GGAPIMethod;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringInterfaceRestSecurityApplierFilter extends OncePerRequestFilter {

	@Autowired
	private IGGAPISecurityEngine security;

	protected HttpServletRequest doFilter(HttpServletRequest request, HttpServletResponse response) throws GGAPIException {
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerFilter.CALLER_ATTRIBUTE_NAME);

		if (caller.getDomain().getSecurity().isAuthenticatorEntity() 
				&& (caller.getAccessRule().getOperation().getMethod() == GGAPIMethod.create || caller.getAccessRule().getOperation().getMethod() == GGAPIMethod.update)) {
			if (log.isDebugEnabled()) {
				log.debug("Applying security on authenticator entity "
						+ caller.getDomain().getEntity().getValue0().getSimpleName());
			}

			try {

				HttpServletRequest httpServletRequest = (HttpServletRequest) request;
				ModifiableHttpServletRequest modifiableRequest = new ModifiableHttpServletRequest(httpServletRequest);

				String originalBody = new String(modifiableRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
				
				ObjectMapper mapper = new ObjectMapper()
						.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
						.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
				
				Object entity = mapper.readValue(originalBody, caller.getDomain().getEntity().getValue0());
				
				this.security.applySecurityOnAuthenticatorEntity(caller, entity, new HashMap<String, String>());

				String writeValueAsString = mapper.writeValueAsString(entity);
				modifiableRequest.setRequestBody(writeValueAsString);
				
				return modifiableRequest;
				
			} catch (IOException e) {
				throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, e.getMessage());
			}

		} else {
			return request;
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (((HttpServletRequest) request).getServletPath().startsWith("/api")) {
			try {
				HttpServletRequest requestModified = this.doFilter(request, response);
				filterChain.doFilter(requestModified, response);
			} catch (GGAPIException e) {
				GGAPIServiceResponse responseObject = new GGAPIServiceResponse(e.getMessage(), GGAPIServiceResponseCode.fromExceptionCode(e));		
				ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);
				String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());
				((HttpServletResponse) response).setStatus(responseEntity.getStatusCode().value());
				response.setContentType("application/json");
				((HttpServletResponse) response).getWriter().write(json);
				((HttpServletResponse) response).getWriter().flush();
				return;
			}
		} else {
			filterChain.doFilter(request, response);
		}
	}
}
