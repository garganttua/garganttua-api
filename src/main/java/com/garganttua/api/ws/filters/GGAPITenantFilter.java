package com.garganttua.api.ws.filters;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.IGGAPITenant;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service("tenantFilter")
public class GGAPITenantFilter extends GGAPIFilter {
	
	@Value(value = "${com.garganttua.api.security.tenantIdHeaderName:tenantId}")
	private String tenantIdHeaderName = "tenantId";
	
	@Value(value = "${com.garganttua.api.security.requestedTenantIdHeaderName:requestedTenantId}")
	private String requestedTenantIdHeaderName = "requestedTenantId";
	
	@Value(value = "${com.garganttua.api.superTenantId:0}")
	private String superTenantId = "0";

	private Optional<IGGAPIController<IGGAPITenant, ? extends IGGAPIDTOObject<IGGAPITenant>>> tenantsController;
	
	@Override
	public void setEngine(IGGAPIEngine engine) {
		super.setEngine(engine);
		this.tenantsController = Optional.ofNullable(this.engine.getTenantsControllerAccessor().getTenantsController());
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		super.doFilter(request, response, chain);
		GGAPICaller caller = this.getCaller(request);
		IGGAPIAccessRule accessRule = caller.getAccessRule();
		
		if( accessRule != null && caller.getTenantId() == null) {
			
			if( accessRule != null && accessRule.getAccess() == GGAPICrudAccess.anonymous ) {
				caller.setAnonymous(true);
			}
			
			String tenantId = ((HttpServletRequest) request).getHeader(this.tenantIdHeaderName);
			String requestedtenantId = ((HttpServletRequest) request).getHeader(this.requestedTenantIdHeaderName);

			if( (tenantId == null || tenantId.isEmpty()) && (accessRule != null && accessRule.getAccess() != GGAPICrudAccess.anonymous) ) {
				((HttpServletResponse) response).setStatus(400);
				response.getWriter().write("No header "+this.tenantIdHeaderName+" found");
				response.getWriter().flush();
				return;
			}
		
			caller.setTenantId(tenantId);
			caller.setRequestedTenantId(requestedtenantId);			
			
			if( tenantId != null && tenantId.equals(this.superTenantId) ) {
				caller.setSuperTenant(true);
			}
			
			if( this.tenantsController.isPresent() && tenantId != null) {
				try {
					GGAPICaller superCaller = new GGAPICaller();
					superCaller.setSuperTenant(true);
					superCaller.setTenantId(this.superTenantId);
					superCaller.setRequestedTenantId(tenantId);

					IGGAPITenant tenant = this.tenantsController.get().getEntity(superCaller, tenantId, null);
					caller.setSuperTenant(tenant.isSuperTenant());
				} catch (GGAPIEntityException e) {
					((HttpServletResponse) response).setStatus(e.getHttpErrorCode().value());
					response.getWriter().write(e.getMessage());
					response.getWriter().flush();
					return;
				}
			} 
			this.setRequestedTenantId(caller, accessRule);
		
		}
		chain.doFilter(request, response);
	}
	
	public void setRequestedTenantId(GGAPICaller caller, IGGAPIAccessRule accessRule) {
        if (caller.isSuperTenant()) {
            if (caller.getRequestedTenantId() == null) {
                if (accessRule.getHttpMethod() != HttpMethod.GET) {
                    caller.setRequestedTenantId(caller.getTenantId());
                }
            }
        } else {
            if (caller.getRequestedTenantId() == null) {
                caller.setRequestedTenantId(caller.getTenantId());
            }
        }
    }

}
