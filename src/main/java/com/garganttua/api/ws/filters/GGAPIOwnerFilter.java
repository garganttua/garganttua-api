package com.garganttua.api.ws.filters;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.IGGAPIOwner;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Service("ownerFilter")
public class GGAPIOwnerFilter extends GGAPIFilter {
	
	@Value(value = "${com.garganttua.api.security.ownerIdHeaderName:ownerId}")
	private String ownerIdHeaderName = "ownerId";
	
	@Value(value = "${com.garganttua.api.superOnwerId:0}")
	private String superOwnerId = "0";
	
	@Value(value = "${com.garganttua.api.superTenantId:0}")
	private String superTenantId = "0";

	private Optional<IGGAPIController<IGGAPIOwner, ? extends IGGAPIDTOObject<IGGAPIOwner>>> ownersController;
	
	@Override
	public void setEngine(IGGAPIEngine engine) {
		super.setEngine(engine);
		this.ownersController = Optional.ofNullable(this.engine.getOwnerControllerAccessor().getOwnersController());
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		super.doFilter(request, response, chain);
		GGAPICaller caller = this.getCaller(request);
		
		if( caller.getOwnerId() == null ) {
			IGGAPIAccessRule accessRule = caller.getAccessRule();
			String ownerId = ((HttpServletRequest) request).getHeader(this.ownerIdHeaderName);
			
			if( accessRule != null && accessRule.getAccess() == GGAPICrudAccess.owner ) {
				if( ownerId == null || ownerId.isEmpty() ) {
					throw new IOException("No header "+this.ownerIdHeaderName+" found");
				}
			}
			
			if( ownerId != null && !ownerId.isEmpty() ) {
				caller.setOwnerId(ownerId);
					
				if( this.ownersController.isPresent() ) {
						try {
							GGAPICaller superCaller = new GGAPICaller();
							superCaller.setSuperTenant(true);
							superCaller.setTenantId(this.superTenantId);
							IGGAPIOwner owner = this.ownersController.get().getEntity(superCaller, ownerId);
							caller.setOwnerId(owner.getOwnerId());
							caller.setSuperOwner(owner.isSuperOnwer());
						} catch (GGAPIEntityException e) {
							throw new IOException(e);
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