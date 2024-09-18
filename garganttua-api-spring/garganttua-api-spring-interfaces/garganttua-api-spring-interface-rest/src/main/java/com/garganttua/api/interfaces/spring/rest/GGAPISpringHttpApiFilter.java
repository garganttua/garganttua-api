package com.garganttua.api.interfaces.spring.rest;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.service.GGAPIServiceResponse;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class GGAPISpringHttpApiFilter extends OncePerRequestFilter {
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
	
		if (((HttpServletRequest) request).getServletPath().startsWith("/api")) {
			try {
				this.doFilter(request, response);
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
		}

		filterChain.doFilter(request, response);
	}

	protected abstract void doFilter(HttpServletRequest request, HttpServletResponse response) throws GGAPIException ;

}
