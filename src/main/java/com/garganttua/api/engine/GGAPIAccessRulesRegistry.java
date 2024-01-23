package com.garganttua.api.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.garganttua.api.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.security.authentication.ws.GGAPIAuthoritiesRestService;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

@Service
public class GGAPIAccessRulesRegistry implements IGGAPIAccessRulesRegistry {
	
	@Autowired
	private IGGAPIServicesRegistry servicesRegistry;
	
	@Autowired
	private Optional<GGAPIAuthoritiesRestService> authoritiesWS;
	
	@Getter
	private List<IGGAPIAccessRule> accessRules = new ArrayList<IGGAPIAccessRule>();

	@PostConstruct
	private void init() {
		this.servicesRegistry.getServices().forEach( service -> {
			accessRules.addAll(service.createAccessRules());
		});
		this.authoritiesWS.ifPresent(ws -> {
			accessRules.addAll(ws.getCustomAuthorizations());
		});
	}
	
	@Override
	public IGGAPIAccessRule getAccessRule(HttpServletRequest request) {
		String methodStr = request.getMethod();

		HttpMethod method = null;
		String uri = request.getRequestURI();

		switch (methodStr) {
		case "GET":
			method = HttpMethod.GET;
			break;
		case "POST":
			method = HttpMethod.POST;
			break;
		case "PATCH":
			method = HttpMethod.PATCH;
			break;
		case "DELETE":
			method = HttpMethod.DELETE;
			break;
		}
		String uriTotest = uri;
		String[] uriParts = uri.split("/");
		
		if (uriParts.length > 2) {
			uriTotest = "/" + uriParts[1] + "/*";
		}

		for (IGGAPIAccessRule auth : this.accessRules) {
			if (auth.getEndpoint().equals(uriTotest) && auth.getHttpMethod() == method) {
				return auth;
			}
		}

		return null;
	}

}
