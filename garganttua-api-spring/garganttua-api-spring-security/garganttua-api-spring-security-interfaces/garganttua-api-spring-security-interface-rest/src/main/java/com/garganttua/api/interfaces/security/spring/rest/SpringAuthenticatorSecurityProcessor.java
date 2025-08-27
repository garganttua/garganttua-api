package com.garganttua.api.interfaces.security.spring.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.service.ServiceResponse;
import com.garganttua.api.interfaces.spring.rest.CallerFilter;
import com.garganttua.api.interfaces.spring.rest.ServiceResponseUtils;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.Method;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.security.ISecurityEngine;
import com.garganttua.api.spec.service.ServiceResponseCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SpringAuthenticatorSecurityProcessor extends OncePerRequestFilter {

	@Autowired
	private ISecurityEngine security;

	protected HttpServletRequest doBeforeFilter(HttpServletRequest request, HttpServletResponse response)
			throws CoreException {
		ICaller caller = (ICaller) request.getAttribute(CallerFilter.CALLER_ATTRIBUTE_NAME);

		if (caller.isAuthenticatorDomain()
				&& (caller.getMethod() == Method.create
						|| caller.getMethod() == Method.update)) {
			if (log.isDebugEnabled()) {
				log.debug("Pre processing security on authenticator entity "
						+ caller.getDomainEntityClass().getSimpleName());
			}

			try {
				ModifiableHttpServletRequest modifiableRequest = new ModifiableHttpServletRequest(request);

				String originalBody = new String(modifiableRequest.getInputStream().readAllBytes(),
						StandardCharsets.UTF_8);

				ObjectMapper mapper = new ObjectMapper()
						.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
						.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

				Object entity = mapper.readValue(originalBody, caller.getDomainEntityClass());

				this.security.authenticatorEntitySecurityPreProcessing(caller, entity, new HashMap<String, String>());

				String writeValueAsString = mapper.writeValueAsString(entity);
				modifiableRequest.setRequestBody(writeValueAsString);

				return modifiableRequest;

			} catch (IOException e) {
				throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR, e.getMessage());
			}

		} else {
			return request;
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (request.getServletPath().startsWith("/api")) {
			try {
				request = this.doBeforeFilter(request, response);
			} catch (CoreException e) {
				ServiceResponse responseObject = new ServiceResponse(e.getMessage(),
						ServiceResponseCode.fromExceptionCode(e));
				ResponseEntity<?> responseEntity = ServiceResponseUtils.toResponseEntity(responseObject);
				String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());
				((HttpServletResponse) response).setStatus(responseEntity.getStatusCode().value());
				response.setContentType("application/json");
				((HttpServletResponse) response).getWriter().write(json);
				((HttpServletResponse) response).getWriter().flush();
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}
