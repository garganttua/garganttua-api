package com.garganttua.api.interfaces.spring.rest.old;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//@Service("ownerFilter")
public class GGAPIOwnerFilter extends GGAPIFilter {
	
	@Value(value = "${com.garganttua.api.security.ownerIdHeaderName:ownerId}")
	private String ownerIdHeaderName = "ownerId";
	
	@Value(value = "${com.garganttua.api.superOnwerId:0}")
	private String superOwnerId = "0";
	
	@Value(value = "${com.garganttua.api.superTenantId:0}")
	private String superTenantId = "0";

	private Optional<IGGAPIDomain> ownersDomain;

	private IGGAPIEntityFactory<?> factory;

	@Override
	public void setEngine(IGGAPIEngine engine) {
		super.setEngine(engine);
		this.ownersDomain = Optional.ofNullable(this.engine.getOwnerDomain());
		if( this.ownersDomain.isPresent() ) {
			this.factory = this.engine.getFactoriesRegistry().getFactory(this.ownersDomain.get().getEntity().getValue1().domain());
		}
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		super.doFilter(request, response, chain);
		GGAPICaller caller = (GGAPICaller) this.getCaller(request);
		IGGAPIDomain domain = this.getDomain((HttpServletRequest) request);
		
		if( caller.getOwnerId() == null ) {
			IGGAPIAccessRule accessRule = caller.getAccessRule();
			String ownerId = ((HttpServletRequest) request).getHeader(this.ownerIdHeaderName);
			
			HttpMethod method = this.getHttpMethod(request);
			
			if( (accessRule != null && accessRule.getAccess() == GGAPIServiceAccess.owner) || (domain != null && domain.getEntity().getValue1().ownedEntity() && (method == HttpMethod.POST || method == HttpMethod.PATCH)  ) ) {
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
//						try {
//							Object owner = this.factory.getEntityFromRepository(caller, null, GGAPIEntityIdentifier.UUID, ownerId);
//							IGGObjectQuery q = GGObjectQueryFactory.objectQuery(this.ownersDomain.get().getEntity().getValue0(), owner);
							
//							caller.setOwnerId((String) q.getValue(this.ownersDomain.get().getEntity().getValue1().ownerIdFieldAddress()));
//							caller.setSuperOwner((boolean) q.getValue(this.ownersDomain.get().getEntity().getValue1().superOnwerIdFieldAddress()));
//						} catch ( GGReflectionException e) {
//							((HttpServletResponse) response).setStatus(GGAPIHttpErrorCodeTranslator.getHttpErrorCode(e).value());
//							response.getWriter().write(e.getMessage());
//							response.getWriter().flush();
//							return;
//						}
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