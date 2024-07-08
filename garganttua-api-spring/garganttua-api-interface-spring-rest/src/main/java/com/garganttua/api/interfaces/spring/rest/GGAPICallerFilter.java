package com.garganttua.api.interfaces.spring.rest;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.service.GGAPIServiceResponse;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service("callerFilter")
public class GGAPICallerFilter implements Filter {

	public static final String CALLER_ATTRIBUTE_NAME = "caller";

	@Value(value = "${com.garganttua.api.interface.spring.rest.ownerIdHeaderName:ownerId}")
	private String tenantIdHeaderName = "tenantId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.requestedTenantIdHeaderName:requestedTenantId}")
	private String requestedTenantIdHeaderName = "requestedTenantId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.ownerIdHeaderName:ownerId}")
	private String ownerIdHeaderName = "ownerId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.superOnwerId:0}")
	private String superOwnerId = "0";

	@Value(value = "${com.garganttua.api.interface.spring.rest.superTenantId:0}")
	private String superTenantId = "0";

	@Autowired
	private IGGAPIEngine engine;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		GGAPICaller attribute = new GGAPICaller();

		String tenantId = ((HttpServletRequest) request).getHeader(this.tenantIdHeaderName);
		String requestedtenantId = ((HttpServletRequest) request).getHeader(this.requestedTenantIdHeaderName);
		String ownerId = ((HttpServletRequest) request).getHeader(this.ownerIdHeaderName);

		if (!this.doTenantIdFiltering(attribute, tenantId, requestedtenantId, (HttpServletResponse) response))
			return;

		request.setAttribute(CALLER_ATTRIBUTE_NAME, attribute);
		chain.doFilter(request, response);
	}

	private boolean doTenantIdFiltering(GGAPICaller caller, String tenantId, String requestedtenantId,
			HttpServletResponse response) throws IOException {
		Optional<IGGAPIDomain> tenantsDomain = Optional.ofNullable(this.engine.getTenantDomain());
		IGGAPIEntityFactory<?> factory = null;
		
		if (tenantsDomain.isPresent()) {
			factory = this.engine.getFactoriesRegistry()
					.getFactory(tenantsDomain.get().getEntity().getValue1().domain());
		}

		caller.setTenantId(tenantId);
		if (requestedtenantId != null && !requestedtenantId.isEmpty()) {
			caller.setRequestedTenantId(requestedtenantId);
		} else {
			caller.setRequestedTenantId(tenantId);
		}

		if (tenantId != null && tenantId.equals(this.superTenantId)) {
			caller.setSuperTenant(true);
		}

		if (tenantsDomain.isPresent() && tenantId != null) {
			try {
				GGAPICaller superCaller = new GGAPICaller();
				superCaller.setTenantId(tenantId);

				Object tenant = factory.getEntityFromRepository(caller, null, GGAPIEntityIdentifier.UUID, tenantId);

				caller.setSuperTenant((boolean) GGObjectQueryFactory.objectQuery(tenant).getValue(tenantsDomain.get().getEntity().getValue1().superTenantFieldAddress()));

				if (!caller.getTenantId().equals(caller.getRequestedTenantId())) {
					superCaller.setTenantId(requestedtenantId);
					factory.getEntityFromRepository(caller, null, GGAPIEntityIdentifier.UUID, caller.getRequestedTenantId());
				}

			} catch (GGAPIException e) {		
				GGAPIServiceResponse responseObject = new GGAPIServiceResponse(e.getMessage(), GGAPIServiceResponseCode.NOT_FOUND);
				ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);
				
				String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());
				
				response.setStatus(responseEntity.getStatusCode().value());
				response.setContentType("application/json");
				response.getWriter().write(json);
				response.getWriter().flush();
				return false;
			} catch (GGReflectionException e) {
				
				GGAPIException apiException = GGAPIException.findFirstInException(e);
				if( apiException != null ) {
					GGAPIServiceResponse responseObject = new GGAPIServiceResponse(apiException.getMessage(), GGAPIServiceResponseCode.NOT_FOUND);
					ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);
					
					String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());
					
					response.setStatus(responseEntity.getStatusCode().value());
					response.setContentType("application/json");
					response.getWriter().write(json);
					response.getWriter().flush();
					return false;
				} else {
					GGAPIServiceResponse responseObject = new GGAPIServiceResponse(e.getMessage(), GGAPIServiceResponseCode.SERVER_ERROR);
					ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);
					
					String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());
					
					response.setStatus(responseEntity.getStatusCode().value());
					response.setContentType("application/json");
					response.getWriter().write(json);
					response.getWriter().flush();
					return false;
				}
			}
		}

		if (!caller.isSuperTenant()) {
			if (caller.getRequestedTenantId() == null) {
				caller.setRequestedTenantId(caller.getTenantId());
			}
		} 
		
		return true;
	}
}
