package com.garganttua.api.service.rest.filters;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.GGAPIServiceAccess;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.factory.GGAPIEntityIdentifier;
import com.garganttua.api.core.entity.factory.IGGAPIEntityFactory;
import com.garganttua.api.core.entity.interfaces.IGGAPITenant;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;
import com.garganttua.api.service.rest.GGAPIServiceMethodToHttpMethodBinder;

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

	private Optional<GGAPIDynamicDomain> tenantsDomain;

	private IGGAPIEntityFactory factory;

	@Override
	public void setEngine(IGGAPIEngine engine) {
		super.setEngine(engine);
		this.tenantsDomain = Optional.ofNullable(this.engine.getTenantDomain());
		this.factory = this.engine.getEntityFactory();
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		super.doFilter(request, response, chain);
		GGAPICaller caller = this.getCaller(request);
		IGGAPIAccessRule accessRule = caller.getAccessRule();
		
		if( accessRule != null && caller.getTenantId() == null) {
			
			if( accessRule != null && accessRule.getAccess() == GGAPIServiceAccess.anonymous ) {
				caller.setAnonymous(true);
			}
			
			String tenantId = ((HttpServletRequest) request).getHeader(this.tenantIdHeaderName);
			String requestedtenantId = ((HttpServletRequest) request).getHeader(this.requestedTenantIdHeaderName);

			if( (tenantId == null || tenantId.isEmpty()) && (accessRule != null && accessRule.getAccess() != GGAPIServiceAccess.anonymous) ) {
				((HttpServletResponse) response).setStatus(400);
				response.getWriter().write("No header "+this.tenantIdHeaderName+" found");
				response.getWriter().flush();
				return;
			}
		
			caller.setTenantId(tenantId);
			if( requestedtenantId != null && !requestedtenantId.isEmpty() ) {
				caller.setRequestedTenantId(requestedtenantId);	
			} else {
				caller.setRequestedTenantId(tenantId);
			}
			
			if( tenantId != null && tenantId.equals(this.superTenantId) ) {
				caller.setSuperTenant(true);
			}
			
			if( this.tenantsDomain.isPresent() && tenantId != null) {
				try {
					GGAPICaller superCaller = new GGAPICaller();
					superCaller.setTenantId(tenantId);
					
					IGGAPITenant tenant = this.factory.getEntityFromRepository(this.tenantsDomain.get(), superCaller, null, GGAPIEntityIdentifier.UUID, caller.getTenantId());
					caller.setSuperTenant(tenant.isSuperTenant());
					
					if( !caller.getTenantId().equals(caller.getRequestedTenantId()) ) {
						superCaller.setTenantId(requestedtenantId);
						this.factory.getEntityFromRepository(this.tenantsDomain.get(), superCaller, null, GGAPIEntityIdentifier.UUID, caller.getRequestedTenantId());
					}
					
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
                if (GGAPIServiceMethodToHttpMethodBinder.fromServiceMethod(accessRule.getMethod()) != HttpMethod.GET) {
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
