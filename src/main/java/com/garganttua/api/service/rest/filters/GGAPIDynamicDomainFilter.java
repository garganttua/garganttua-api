package com.garganttua.api.service.rest.filters;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.engine.GGAPIDomain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Service("dynamicDomainFilter")
public class GGAPIDynamicDomainFilter extends GGAPIFilter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		super.doFilter(request, response, chain);
		GGAPICaller caller = this.getCaller(request);
		GGAPIDomain ddomain = this.engine.getDynamicDomainsRegistry().getDomain(((HttpServletRequest)request));

		caller.setDomain(ddomain);
		
		chain.doFilter(request, response);
	}
}
