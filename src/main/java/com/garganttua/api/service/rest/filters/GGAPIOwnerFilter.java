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
import com.garganttua.api.core.entity.interfaces.IGGAPIOwner;
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

@Service("ownerFilter")
public class GGAPIOwnerFilter extends GGAPIFilter {
	
	@Value(value = "${com.garganttua.api.security.ownerIdHeaderName:ownerId}")
	private String ownerIdHeaderName = "ownerId";
	
	@Value(value = "${com.garganttua.api.superOnwerId:0}")
	private String superOwnerId = "0";
	
	@Value(value = "${com.garganttua.api.superTenantId:0}")
	private String superTenantId = "0";

	private Optional<GGAPIDynamicDomain> ownersDomain;

	private IGGAPIEntityFactory factory;

	@Override
	public void setEngine(IGGAPIEngine engine) {
		super.setEngine(engine);
		this.ownersDomain = Optional.ofNullable(this.engine.getOwnerDomain());
		this.factory = this.engine.getEntityFactory();
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		super.doFilter(request, response, chain);
		GGAPICaller caller = this.getCaller(request);
		GGAPIDynamicDomain domain = this.getDomain(request);
		
		if( caller.getOwnerId() == null ) {
			IGGAPIAccessRule accessRule = caller.getAccessRule();
			String ownerId = ((HttpServletRequest) request).getHeader(this.ownerIdHeaderName);
			
			HttpMethod method = this.getHttpMethod(request);
			
			if( (accessRule != null && accessRule.getAccess() == GGAPIServiceAccess.owner) || (domain != null && domain.ownedEntity && (method == HttpMethod.POST || method == HttpMethod.PATCH)  ) ) {
				if( ownerId == null || ownerId.isEmpty() ) {
					((HttpServletResponse) response).setStatus(400);
					response.getWriter().write("No header "+this.ownerIdHeaderName+" found");
					response.getWriter().flush();
					return;
				}
			}
			
			if( ownerId != null && !ownerId.isEmpty() ) {
				caller.setOwnerId(ownerId);
					
				if( this.ownersDomain.isPresent() ) {
						try {
							IGGAPIOwner owner = this.factory.getEntityFromRepository(this.ownersDomain.get(), caller, null, GGAPIEntityIdentifier.UUID, ownerId);
							caller.setOwnerId(owner.getOwnerId());
							caller.setSuperOwner(owner.isSuperOnwer());
						} catch (GGAPIEntityException e) {
							((HttpServletResponse) response).setStatus(e.getHttpErrorCode().value());
							response.getWriter().write(e.getMessage());
							response.getWriter().flush();
							return;
						}
				} else {
					if( ownerId.equals(this.superOwnerId) ) {
						caller.setSuperOwner(true);
					}
				}
			}
		}

		chain.doFilter(request, response);
	}

}